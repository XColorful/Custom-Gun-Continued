package dev.xcolorful.customgun.client.model.bake;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.input.player.DebugKey;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
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
 *     <li>[1.20.1, 1.21.6) 只支持即时模式：{@code RenderType} 的 setup/clear 包住 draw，与 {@code BufferSource.endBatch} 的做法一致</li>
 *     <li>[1.21.6, )绘制自己开 {@code RenderPass}：管线用 CGC 的烘焙管线，状态与贴图仍由 {@code RenderType} 的 setup/clear 提</li>
 *     <li>光照需要原地改写的网格（渲染重构B1-c-B，见 {@link #bakePatchable}）额外保留一份 CPU 副本</li>
 *     <li>光照变化时改写其中的 uv2 并 (重传原 VBO / 1.21.6 整段写回原缓冲)，几何本身不再重新发射</li>
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
    private static final List<ByteBufferBuilder> BUILDERS = new ArrayList<>();
    private static int builderDepth;

    /**
     * {@code LightTexture.pack(15, 15)}：发光节点（{@code illuminated}）烘进顶点的固定满亮。
     * 打包格式随版本变化，走项目包装而不是写死数值
     */
    private static final int FULL_BRIGHT = ClientRenderUtils.LightTexture_.pack(15, 15);
    @ApiStatus.AvailableSince("1.21.6")
    private static final Vector4f WHITE = new Vector4f(1.0F, 1.0F, 1.0F, 1.0F);

    // [1.21.6, )
//    private static final ByteBuffer NORMAL_SCRATCH = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder());
    /**
     * 法线矩阵块的内容：烘焙网格存模型空间法线，而原版 {@code entity.vsh} 直接用顶点里的 {@code Normal}
     * （即时块在 CPU 用 {@code IT(姿态)} 写入）。{@code DynamicTransforms} 的 {@code ModelViewMat} 是 mv × 姿态，
     * 取逆转置会少一次摄像机旋转 —— 枪体法线冻结在固定朝向，转视角亮度不变。所以单独走这个 UBO
     */
    private static @Nullable Object normalUniformBuffer; // GpuBuffer
    @ApiStatus.AvailableSince("1.21.6")
    private static Object normalUniformBuffer() {
        return null;
//        GpuBuffer buffer = normalUniformBuffer;
//        if (buffer == null || buffer.isClosed()) {
//            buffer = normalUniformBuffer = RenderSystem.getDevice().createBuffer(
//                    () -> "CGC baked normal matrix", GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, 64);
//        }
//        return buffer;
    }

    // [1.20.1, 1.21.6)
    private final @Nullable VertexBuffer vertexBuffer;

    // [1.21.6, )
//    private final @Nullable GpuBuffer vertexBuffer;
//    private final int indexCount;
//    private final VertexFormat.Mode mode;

    private final int bytes;
    /**
     * 本次烘焙实际写入的是不是扩展顶点格式
     * <ul>
     *     <li>光影包会把 {@code NEW_ENTITY} 换成{@code IrisVertexFormats.ENTITY}</li>
     *     <li>绘制前要和当前光影状态比对：两者不一致说明光影包刚刚切换、缓存里的顶点布局已经不能用了（本帧必须退回即时管线）</li>
     * </ul>
     * <ul>
     *     [1.21.6, )
     *     <li>{@code RenderType#format()} 直接取自 {@code RenderPipeline#getVertexFormat()}</li>
     *     <li>而 Iris 扩展的正是这个 getter，声明的格式与写入的格式恒为同一个对象，这里因此恒为 {@code false}</li>
     *     <li>布局比对改用 {@link #stride()}（见 {@link StaticBake}），本字段保留给 [1.20.1, 1.21.6) 的实现</li>
     * </ul>
     */
//    @Deprecated(since = "1.21.6")
    private final boolean extendedFormat;
    /**
     * 交错顶点字节的 CPU 副本，光照变化时改写其中的 uv2 后再 (整段重传 / 1.21.6 整段写回)
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
                      @Nullable ByteBuffer vertexCopy, int vertexCount, int stride, int uv2Offset,
//                      int indexCount, VertexFormat.Mode mode, // [1.21.6, )
                      int light) {
        this.vertexBuffer = vertexBuffer;
        this.bytes = bytes;
        this.extendedFormat = extendedFormat;
        this.vertexCopy = vertexCopy;
        this.vertexCount = vertexCount;
        this.stride = stride;
        this.uv2Offset = uv2Offset;
//        this.indexCount = indexCount;
//        this.mode = mode;
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
            {
                emitter.accept(builder);
            }
            MeshData rendered = builder.build();
            ClientRenderProfiler.end(ClientRenderProfiler.BAKE_EMIT, profileEmit);

            if (rendered == null) return new BakedMesh(null, 0, false,
                    null, 0, 0, -1,
//                    0, mode, // [1.21.6, )
                    light);

            MeshData.DrawState drawState = rendered.drawState();
            int bytes;
            VertexFormat writtenFormat;
            boolean extendedFormat;
            int vertexCount; {
                // [1.20.1, 1.21.1)
                /*
                1.20.1 drawState.bufferSize()
                sequential 输出没有独立索引缓冲，只算顶点字节
                 */
//                bytes = drawState.bufferSize();
//                writtenFormat = drawState.format();
//                extendedFormat = writtenFormat != format;
//                vertexCount = drawState.vertexCount();

                // [1.21.1, )
                writtenFormat = drawState.format();
                extendedFormat = writtenFormat != format;
                vertexCount = drawState.vertexCount();
                bytes = vertexCount * writtenFormat.getVertexSize();

            }
            int stride = writtenFormat.getVertexSize();
            int uv2Offset = light == FULL_BRIGHT ? -1 : uv2Offset(writtenFormat);
            /*
            [1.20.1, 1.21.6) 上传会释放 RenderedBuffer/MeshData，CPU 副本必须在 upload 之前拷出来
            [1.21.6, ) 关闭 MeshData 会归还 ByteBufferBuilder 的区段，CPU 副本必须在建缓冲之前拷出来
             */
            ByteBuffer vertexCopy = !keepLightCopy || uv2Offset < 0 ? null : copyVertices(rendered.vertexBuffer());

            VertexBuffer vertexBuffer; // GpuBuffer vertexBuffer;
            {
                // [1.20.1, 1.21.6)
                long profileBuffer = ClientRenderProfiler.begin();
                vertexBuffer = new VertexBuffer(
                        // [1.20.1, 1.21.4)
                        VertexBuffer.Usage.STATIC
                        // [1.21.4, )
//                        BufferUsage.STATIC_WRITE
                );
                // VertexBuffer.upload 要求 VAO 已绑定，原版 BufferUploader 同样先 bind 再 upload
                vertexBuffer.bind();
                ClientRenderProfiler.end(ClientRenderProfiler.BAKE_BUFFER, profileBuffer);

                // [1.21.6, )
            }

            long profileUpload = ClientRenderProfiler.begin();
            try {
                // [1.20.1, 1.21.6)
                vertexBuffer.upload(rendered);

                // [1.21.6, )
//                vertexBuffer = RenderSystem.getDevice().createBuffer(
//                        () -> "CGC baked mesh",
//                        GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST,
//                        rendered.vertexBuffer());
            } finally {
                {
                    // [1.20.1, 1.21.6)
                    VertexBuffer.unbind();

                    // [1.21.6, )
//                    rendered.close();
                }
                ClientRenderProfiler.end(ClientRenderProfiler.BAKE_UPLOAD, profileUpload);
                ClientRenderProfiler.countBakeBytes(bytes);
            }
            return new BakedMesh(vertexBuffer, bytes, extendedFormat,
                    vertexCopy, vertexCount, stride, uv2Offset,
//                    drawState.indexCount(), drawState.mode(), // [1.21.6, )
                    light);
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
            if (ClientRenderUtils.getUsage(element) == VertexFormatElement.Usage.UV
                    && ClientRenderUtils.getIndex(element) == 2) return offset;
            offset += ClientRenderUtils.getByteSize(element);
        }
        return -1;
    }

    /**
     * 把网格的 uv2 改写成新的光照，并 (整段重传原 VBO / 1.21.6 整段写回原缓冲)
     * <ul>
     *     <li>不新建缓冲、不重新发射顶点</li>
     *     <li>发光节点烘的是满亮 15/15，不跟着世界光照变化；只有等于旧光照的顶点才会被改写</li>
     * </ul>
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
     * <ul>
     *     [1.20.1, 1.21.6)
     *     <li>重传 {@link #vertexCopy}：VAO 里保存的 attribute 指针本来就指向这份网格自己的 VBO</li>
     *     <li>取回该 VBO 的 id 后整段覆盖即可，顶点格式与 attribute 绑定都不变</li>
     * </ul>
     * <ul>
     *     [1.21.6, )
     *     <li>整段写回 {@link #vertexCopy}：缓冲创建时带了 {@code COPY_DST}，用一次拷贝命令覆盖即可</li>
     *     <li>顶点数与布局都不变</li>
     * </ul>
     */
    private boolean uploadCopy() {
        if (this.vertexBuffer == null || this.vertexCopy == null) return false;
        {
            // [1.20.1, 1.21.6)
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

            // [1.21.6, )
//            RenderSystem.getDevice().createCommandEncoder()
//                    .writeToBuffer(this.vertexBuffer.slice(), this.vertexCopy.duplicate());
//            return true;
        }
    }

    /**
     * <ul>
     *     <li>1.21.1 {@code BufferBuilder} 把 mode/format 收进了构造函数、不再有 {@code begin()}</li>
     *     <li>池子改成缓存 {@code ByteBufferBuilder}，每次烘焙 {@code new BufferBuilder}(池中的 builder, mode, format)</li>
     *     <li>[1.20.1, 1.21.6) {@code VertexBuffer.upload} 会 close 掉 {@code MeshData}，进而归还 {@code ByteBufferBuilder} 的区段，池可以安全复用</li>
     *     <li>[1.21.6, ) {@code MeshData.close()} 会归还 {@code ByteBufferBuilder} 的区段，池可以安全复用</li>
     * </ul>
     */
    private static BufferBuilder acquireBuilder(VertexFormat.Mode mode, VertexFormat format) {
        int depth = builderDepth++;
        if (depth == BUILDERS.size()) BUILDERS.add(
                new ByteBufferBuilder(BUILD_CAPACITY)
        );
        // [1.20.1, 1.21.1)
//        return BUILDERS.get(depth);

        // [1.21.1, )
        return new BufferBuilder(BUILDERS.get(depth), mode, format);
    }

    /**
     * @param pose 模型空间 → 当前视图空间的变换，即调用方的 PoseStack 顶矩阵
     * @return {@code false} 表示没有可用的着色器（自定义 shader 未注册且没有光影包）或管线（自定义管线未注册），调用方应退回即时管线
     */
    public boolean draw(RenderType renderType, Matrix4f pose) {
        if (this.closed) return false;
        if (this.vertexBuffer == null) return true;

//        @Nullable RenderPipeline pipeline; // [1.21.6, )
        Matrix4f modelView; {
            // [1.20.1, 1.21.1)
//            modelView = new Matrix4f(ClientRenderHelper.GL._getModelViewMatrix()).mul(pose);

            // [1.21.1, 1.21.6)
            /*
            1.21.1 起 RenderSystem 的 modelview 不是单位阵（手部渲染时是摄像机旋转的逆）
            这里只取 modelview 本身，绘制时并进投影矩阵
            ModelViewMat 只放调用方姿态，理由见下方绘制段
             */
            modelView = new Matrix4f(ClientRenderHelper.GL._getModelViewMatrix());

            // [1.21.6, )
//            @Nullable RenderPipeline pipeline = ClientRenderRegistry.BakedEntityRenderState.bakedPipelineFor(renderType);
//            if (pipeline == null) return false;
        }


        boolean setup = false;
        try {
            // setupRenderState 才会把该 RenderType 的 (shader / 贴图/lightmap/overlay) 设进 RenderSystem
            renderType.setupRenderState();
            setup = true;

            // [1.20.1, 1.21.6)
            @Nullable ShaderInstance shader = StaticBake.resolveEntityShader();
            if (shader == null) return false;

            this.vertexBuffer.bind();

            // [1.20.1, 1.21.1)
//            this.vertexBuffer.drawWithShader(modelView, RenderSystem.getProjectionMatrix(), shader);

            // [1.21.1, 1.21.6)
            /*
            ModelViewMat 只能放调用方姿态，不能带 RenderSystem 的 modelview：
            原版实体着色器把顶点里的 Normal 当视图空间法线直接点乘光照，即时块的 Normal 就是 CPU 用 IT(姿态) 写进顶点的；
            烘焙块由着色器现算 IT(ModelViewMat)，两者相等才是同一口径
            1.21.1 手部渲染的 modelview 是摄像机旋转的逆（GameRenderer#renderItemInHand），
            带进去会让 IT(ModelViewMat) 少乘一次该旋转，枪体法线冻结在固定朝向：转视角亮度不变，而弹匣（即时块）正常
            把 modelview 并进投影矩阵后位置不变：Proj × mv × 姿态 == (Proj × mv) × 姿态
             */
            // 雾要真实视图坐标，而 ModelViewMat 已交给姿态：modelview 单独走 CgcViewMat
            // 光影包的 program 里没有这个 uniform，取不到就跳过（不能用 safeGetUniform，那会每帧刷警告）
            @Nullable var viewMat = shader.getUniform("CgcViewMat");
            if (viewMat != null) viewMat.set(modelView);
            this.vertexBuffer.drawWithShader(pose,
                    new Matrix4f(RenderSystem.getProjectionMatrix()).mul(modelView),
                    shader);

            // [1.21.6, )
            /* 源码太长，并且跨版本要改，所以用保留commit记录的方式，1.20.1改直接在1.21.6能用
            // 与 RenderType#draw 一致：状态设好之后才取矩阵，layering / texturing 状态可能改过它们
            modelView = new Matrix4f(RenderSystem.getModelViewMatrix()).mul(pose);
            GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(
                    modelView, WHITE, RenderSystem.getModelOffset(),
                    RenderSystem.getTextureMatrix(), RenderSystem.getShaderLineWidth());

            // 法线矩阵单独上传：ModelViewMat 保持 mv × 姿态供位置与雾使用，这里给 IT(姿态)
            // writeToBuffer 不能在 render pass 内调用，必须放在建 pass 之前
            GpuBuffer normalBuffer = normalUniformBuffer();
            ByteBuffer normalData = NORMAL_SCRATCH;
            normalData.clear();
            new Matrix4f(pose).invert().transpose().get(normalData);
            // get(ByteBuffer) 是否推进 position 随 JOML 实现不同，clear() 让写读两端都从 0 开始
            normalData.clear();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(normalBuffer.slice(), normalData);

            RenderTarget target = outputTarget(renderType);
            GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride != null
                    ? RenderSystem.outputColorTextureOverride
                    : target.getColorTextureView();
            GpuTextureView depthTexture = target.useDepth
                    ? (RenderSystem.outputDepthTextureOverride != null
                    ? RenderSystem.outputDepthTextureOverride
                    : target.getDepthTextureView())
                    : null;

            // ModelViewMat 只供位置与雾（mv × 姿态），法线由渲染器另取 RenderSystem 的 modelview 现算：
            // 光影包用的是 Iris 的 iris_NormalMat = IT(modelview)
            // 而烘焙网格存模型空间法线，不把本次姿态并进去就比即时块（CPU 用 IT(姿态) 写顶点 Normal）少一次姿态旋转
            // 导致光影下枪体（烘焙块）因此比弹匣（即时块）暗，且只在 [1.21.6, ) 成立
            // ModelViewMat 本来就是 mv × 姿态，所以并进 modelview 的是同一个矩阵，位置与雾都不变
            // 已知限制：
            // 只对用 gl_NormalMatrix 的光影包等价（Complementary 是）
            // 直接读 gl_Normal 的包不经过这个矩阵，uniform 补不回来，烘焙块法线仍会少一次姿态
            Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
            modelViewStack.pushMatrix().mul(pose);
            try (RenderPass pass = RenderSystem.getDevice()
                    .createCommandEncoder()
                    .createRenderPass(() -> "CGC baked mesh", colorTexture, OptionalInt.empty(), depthTexture, OptionalDouble.empty())) {
                pass.setPipeline(pipeline);
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("DynamicTransforms", transforms);
                pass.setUniform("CgcBaked", normalBuffer);
                {
                    // [1.21.6, 1.21.10)
                    // 与 RenderType#draw 一致：NeoForge 的模板测试只存在于 RenderSystem，必须由每个 pass 自己带上
                    @Nullable var stencilTest = RenderSystem.STENCIL_TEST;  // NeoForge类，但是不触发import
                    if (stencilTest != null) {
                        pass.enableStencilTest(stencilTest);
                    }

                    // [1.21.10, )
                    // 模板测试改由 RenderPipeline 携带：
                    // setPipeline 内部会先经 RenderSystem#applyPipelineModifiers 注入 CGC 累积的模板状态，这里不需要再手动设置
                }
                pass.setVertexBuffer(0, this.vertexBuffer);

                for (int i = 0; i < 12; i++) {
                    GpuTextureView texture = RenderSystem.getShaderTexture(i);
                    if (texture != null) pass.bindSampler("Sampler" + i, texture);
                }

                // 非排序输出没有独立索引缓冲，用平台按 mode 生成的顺序索引
                RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(this.mode);
                pass.setIndexBuffer(indices.getBuffer(this.indexCount), indices.type());
                pass.drawIndexed(0, 0, this.indexCount, 1);
            } finally {
                modelViewStack.popMatrix();
            }
            */

            return true;
        } finally {
            VertexBuffer.unbind(); // 1.21.6移除
            if (setup) renderType.clearRenderState();
        }
    }

    @ApiStatus.AvailableSince("1.21.6")
    private static RenderTarget outputTarget(RenderType renderType) {
        Object outputState = CustomGunClient.getAccessTransformer().getOutputState(renderType);
        return null; // 让IDE保留上面的引用关系
//        if (outputState instanceof RenderStateShard.OutputStateShard output) {
//            return output.getRenderTarget();
//        }
//        return ClientRenderUtils.getMainRenderTarget(Minecraft.getInstance());
    }

    public int bytes() {
        return this.bytes;
    }

//    @Deprecated(since = "1.21.6")
    public boolean extendedFormat() {
        return this.extendedFormat;
    }
    /**
     * [1.21.6, ) 烘焙时的顶点步长，供绘制侧和当前管线要求的布局比对
     */
    @ApiStatus.AvailableSince("1.21.6")
    public int stride() {
        return this.stride;
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
