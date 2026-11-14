package dev.xcolorful.customgun.client.model.bake;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.xcolorful.customgun.client.input.player.DebugKey;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 模型空间的静态烘焙网格
 * <ul>
 *     <li>顶点只生成一次，常驻 {@link VertexBuffer}；每帧只绑定并换模型视图矩阵绘制</li>
 *     <li>实例矩阵不进顶点，因此同一份网格可以被多个展示框 / 掉落物共享</li>
 *     <li>只支持即时模式：{@code RenderType} 的 setup/clear 包住 draw，与 {@code BufferSource.endBatch} 的做法一致</li>
 *     <li>光照需要原地改写的网格（渲染重构B1-c-B，见 {@link #bakePatchable}）额外保留一份 CPU 副本，
 *         光照变化时改写其中的 uv2 并重传原 VBO，几何本身不再重新发射</li>
 * </ul>
 * 失效条件由调用方放进 {@link StaticBakeCache.Key}，本类不判断
 */
@ApiStatus.Internal
public final class BakedMesh implements AutoCloseable {

    /**
     * 单次烘焙的初始顶点缓冲容量；超出时 {@link BufferBuilder} 自行扩容
     */
    private static final int BUILD_CAPACITY = 1024 * 1024;

    /**
     * 复用的 {@link BufferBuilder}，按烘焙嵌套深度取用
     * <ul>
     *     1.20.1
     *     <li>{@code BufferBuilder} 在构造时经 {@code MemoryTracker} 直接申请原生内存（{@code 容量 × 6}）</li>
     *     <li>而释放路径只重置计数、不归还内存：每次烘焙新建一个就是一次几 MiB 的永久泄漏</li>
     *     <li>{@link DebugKey}采集会每帧强制重建网格，几秒内就能把页面文件耗尽（表现是原生内存不足导致的 JVM 崩溃）</li>
     *     <li>正常流程不嵌套，复用同一实例</li>
     * </ul>
     */
    private static final List<BufferBuilder> BUILDERS = new ArrayList<>();
    private static int builderDepth;

    /**
     * {@code LightTexture.pack(15, 15)}：发光节点（{@code illuminated}）烘进顶点的固定满亮。
     * 打包格式随版本变化，走项目包装而不是写死数值
     */
    private static final int FULL_BRIGHT = ClientRenderUtils.LightTexture_.pack(15, 15);

    private final @Nullable VertexBuffer vertexBuffer;
    private final int bytes;
    /**
     * 本次烘焙实际写入的是不是扩展顶点格式
     * <ul>
     *     <li>光影包会把 {@code NEW_ENTITY} 换成{@code IrisVertexFormats.ENTITY}</li>
     *     <li>绘制前要和当前光影状态比对：两者不一致说明光影包刚刚切换、缓存里的顶点布局已经不能用了（本帧必须退回即时管线）</li>
     * </ul>
     */
    private final boolean extendedFormat;
    /**
     * 交错顶点字节的 CPU 副本，光照变化时改写其中的 uv2 后再整段重传
     * <ul>
     *     <li>{@code null} 表示无法原地改写：空网格、格式里没有 uv2、烘焙时光照恰为满亮
     *     （发光节点与普通顶点的 uv2 此时无法区分），或调用方没有要求保留副本</li>
     * </ul>
     */
    private final @Nullable ByteBuffer vertexCopy;
    private final int vertexCount;
    private final int stride;
    /**
     * uv2 在单个顶点里的字节偏移；格式不含 uv2 时为 {@code -1}
     */
    private final int uv2Offset;
    /**
     * 网格当前烘着的 packed 光照
     */
    private int light;
    private boolean closed;

    private BakedMesh(@Nullable VertexBuffer vertexBuffer, int bytes, boolean extendedFormat,
                      @Nullable ByteBuffer vertexCopy, int vertexCount, int stride, int uv2Offset, int light) {
        this.vertexBuffer = vertexBuffer;
        this.bytes = bytes;
        this.extendedFormat = extendedFormat;
        this.vertexCopy = vertexCopy;
        this.vertexCount = vertexCount;
        this.stride = stride;
        this.uv2Offset = uv2Offset;
        this.light = light;
    }

    /**
     * 光照进缓存键的场景（整模静态）：光照变化直接换网格，不需要 CPU 副本
     * @param light   烘进顶点的 packed 光照
     * @param emitter 在模型空间写顶点；写 0 个顶点时得到空网格，绘制为空操作
     */
    public static BakedMesh bake(VertexFormat.Mode mode, VertexFormat format, int light,
                                 Consumer<VertexConsumer> emitter) {
        return bake(mode, format, light, false, emitter);
    }

    /**
     * 光照不进缓存键的场景（第一人称分块）：保留 CPU 副本，供 {@link #updateLight} 原地改写
     */
    public static BakedMesh bakePatchable(VertexFormat.Mode mode, VertexFormat format, int light,
                                          Consumer<VertexConsumer> emitter) {
        return bake(mode, format, light, true, emitter);
    }

    private static BakedMesh bake(VertexFormat.Mode mode, VertexFormat format, int light,
                                  boolean keepLightCopy, Consumer<VertexConsumer> emitter) {
        BufferBuilder builder = acquireBuilder(mode, format);
        long profileEmit = ClientRenderProfiler.begin();
        try {
            builder.begin(mode, format); {
                emitter.accept(builder);
            }
            BufferBuilder.RenderedBuffer rendered = builder.endOrDiscardIfEmpty();
            ClientRenderProfiler.end(ClientRenderProfiler.BAKE_EMIT, profileEmit);

            if (rendered == null) return new BakedMesh(null, 0, false,
                    null, 0, 0, -1, light);

            int bytes;
            VertexFormat writtenFormat;
            boolean extendedFormat;
            int vertexCount; {
                // [1.20.1, 1.21.1)
                /*
                1.20.1 drawState.bufferSize()
                sequential 输出没有独立索引缓冲，只算顶点字节
                 */
                bytes = rendered.drawState().bufferSize();
                writtenFormat = rendered.drawState().format();
                extendedFormat = writtenFormat != format;
                vertexCount = rendered.drawState().vertexCount();

                // [1.21.1, )
//                MeshData.DrawState drawState = rendered.drawState();
//                writtenFormat = drawState.format();
//                extendedFormat = writtenFormat != format;
//                vertexCount = drawState.vertexCount();
//                bytes = vertexCount * writtenFormat.getVertexSize();

            }
            int stride = writtenFormat.getVertexSize();
            int uv2Offset = light == FULL_BRIGHT ? -1 : uv2Offset(writtenFormat);
            // 上传会释放 RenderedBuffer/MeshData，CPU 副本必须在 upload 之前拷出来
            ByteBuffer vertexCopy = !keepLightCopy || uv2Offset < 0 ? null : copyVertices(rendered.vertexBuffer());

            long profileBuffer = ClientRenderProfiler.begin();
            VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
            // VertexBuffer.upload 要求 VAO 已绑定，原版 BufferUploader 同样先 bind 再 upload
            vertexBuffer.bind();
            ClientRenderProfiler.end(ClientRenderProfiler.BAKE_BUFFER, profileBuffer);

            long profileUpload = ClientRenderProfiler.begin();
            try {
                vertexBuffer.upload(rendered);
            } finally {
                VertexBuffer.unbind();
                ClientRenderProfiler.end(ClientRenderProfiler.BAKE_UPLOAD, profileUpload);
                ClientRenderProfiler.countBakeBytes(bytes);
            }
            return new BakedMesh(vertexBuffer, bytes, extendedFormat,
                    vertexCopy, vertexCount, stride, uv2Offset, light);
        } finally {
            builderDepth--;
        }
    }

    private static ByteBuffer copyVertices(ByteBuffer source) {
        // 与 BufferBuilder 保持同一字节序：patch 时按同样的序读写 short
        ByteBuffer copy = ByteBuffer.allocateDirect(source.remaining()).order(source.order());
        copy.put(source);
        copy.position(0);
        return copy;
    }

    /**
     * uv2 元素在顶点里的字节偏移；格式里没有 uv2 时返回 {@code -1}
     */
    private static int uv2Offset(VertexFormat format) {
        int offset = 0;
        for (VertexFormatElement element : format.getElements()) {
            if (ClientRenderUtils.getUsage(element) == VertexFormatElement.Usage.UV && element.getIndex() == 2) return offset;
            offset += ClientRenderUtils.getByteSize(element);
        }
        return -1;
    }

    /**
     * 把网格的 uv2 改写成新的光照，并整段重传原 VBO（不新建缓冲、不重新发射顶点）
     * <br>
     * 发光节点烘的是满亮 15/15，不跟着世界光照变化；只有等于旧光照的顶点才会被改写
     * @return {@code false} 表示这份网格无法原地改写，调用方应关闭它并重新烘焙
     */
    public boolean updateLight(int newLight) {
        if (this.closed) return false;
        if (this.light == newLight) return true;
        if (this.vertexBuffer == null) {
            this.light = newLight;
            return true;
        }
        if (this.vertexCopy == null) {
            ClientRenderProfiler.countLightPatchNoCopy();
            return false;
        }

        long profileLight = ClientRenderProfiler.begin();
        short oldU = (short) (this.light & 0xFFFF);
        short oldV = (short) (this.light >>> 16);
        short newU = (short) (newLight & 0xFFFF);
        short newV = (short) (newLight >>> 16);
        ByteBuffer copy = this.vertexCopy;
        for (int i = 0; i < this.vertexCount; i++) {
            int offset = i * this.stride + this.uv2Offset;
            if (copy.getShort(offset) == oldU && copy.getShort(offset + 2) == oldV) {
                copy.putShort(offset, newU);
                copy.putShort(offset + 2, newV);
            }
        }
        boolean uploaded = this.uploadCopy();
        ClientRenderProfiler.end(ClientRenderProfiler.LIGHT_UPDATE, profileLight);
        if (!uploaded) {
            ClientRenderProfiler.countLightPatchNoVbo();
            return false;
        }
        this.light = newLight;
        ClientRenderProfiler.countLightPatch(copy.capacity());
        return true;
    }

    /**
     * 重传 {@link #vertexCopy}：VAO 里保存的 attribute 指针本来就指向这份网格自己的 VBO，
     * 取回该 VBO 的 id 后整段覆盖即可，顶点格式与 attribute 绑定都不变
     */
    private boolean uploadCopy() {
        if (this.vertexBuffer == null || this.vertexCopy == null) return false;
        this.vertexBuffer.bind();
        try {
            int vbo = GL20.glGetVertexAttribi(0, GL20.GL_VERTEX_ATTRIB_ARRAY_BUFFER_BINDING);
            if (vbo <= 0) return false;
            GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            RenderSystem.glBufferData(GL15.GL_ARRAY_BUFFER, this.vertexCopy, GL15.GL_STATIC_DRAW);
            return true;
        } finally {
            VertexBuffer.unbind();
        }
    }

    /**
     * <ul>
     *     <li>1.21.1 {@code BufferBuilder} 把 mode/format 收进了构造函数、不再有 {@code begin()}</li>
     *     <li>池子改成缓存 {@code ByteBufferBuilder}，每次烘焙 {@code new BufferBuilder}(池中的 builder, mode, format)</li>
     *     <li>{@code VertexBuffer.upload} 会 close 掉 {@code MeshData}，进而归还 {@code ByteBufferBuilder} 的区段，池可以安全复用</li>
     * </ul>
     */
    private static BufferBuilder acquireBuilder(VertexFormat.Mode mode, VertexFormat format) {
        int depth = builderDepth++;
        if (depth == BUILDERS.size()) BUILDERS.add(
                new BufferBuilder(BUILD_CAPACITY) // new ByteBufferBuilder(BUILD_CAPACITY)
        );
        // [1.20.1, 1.21.1)
        return BUILDERS.get(depth);

        // [1.21.1, )
//        return new BufferBuilder(BUILDERS.get(depth), mode, format);
    }

    /**
     * @param pose 模型空间 → 当前视图空间的变换，即调用方的 PoseStack 顶矩阵
     * @return {@code false} 表示没有可用的着色器（自定义 shader 未注册且没有光影包），调用方应退回即时管线
     */
    public boolean draw(RenderType renderType, Matrix4f pose) {
        if (this.closed) return false;
        if (this.vertexBuffer == null) return true;

        Matrix4f modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(pose);
        boolean setup = false;
        try {
            // setupRenderState 才会把该 RenderType 的 shader 设进 RenderSystem
            renderType.setupRenderState();
            setup = true;

            @Nullable ShaderInstance shader = StaticBake.resolveEntityShader();
            if (shader == null) return false;

            this.vertexBuffer.bind();
            this.vertexBuffer.drawWithShader(modelView, RenderSystem.getProjectionMatrix(), shader);
            return true;
        } finally {
            VertexBuffer.unbind();
            if (setup) renderType.clearRenderState();
        }
    }

    public int bytes() {
        return this.bytes;
    }

    public boolean extendedFormat() {
        return this.extendedFormat;
    }

    @Override
    public void close() {
        if (this.closed) return;
        this.closed = true;
        if (this.vertexBuffer != null) {
            this.vertexBuffer.close();
        }
    }
}
