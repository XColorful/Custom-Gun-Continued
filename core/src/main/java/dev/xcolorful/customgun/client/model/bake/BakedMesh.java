package dev.xcolorful.customgun.client.model.bake;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.xcolorful.customgun.client.input.player.DebugKey;
import dev.xcolorful.customgun.client.util.ClientRenderProfiler;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 模型空间的静态烘焙网格
 * <ul>
 *     <li>顶点只生成一次，常驻 {@link VertexBuffer}；每帧只绑定并换模型视图矩阵绘制</li>
 *     <li>实例矩阵不进顶点，因此同一份网格可以被多个展示框 / 掉落物共享</li>
 *     <li>只支持即时模式：{@code RenderType} 的 setup/clear 包住 draw，与 {@code BufferSource.endBatch} 的做法一致</li>
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
    private boolean closed;

    private BakedMesh(@Nullable VertexBuffer vertexBuffer, int bytes, boolean extendedFormat) {
        this.vertexBuffer = vertexBuffer;
        this.bytes = bytes;
        this.extendedFormat = extendedFormat;
    }

    /**
     * @param emitter 在模型空间写顶点；写 0 个顶点时得到空网格，绘制为空操作
     */
    public static BakedMesh bake(VertexFormat.Mode mode, VertexFormat format, Consumer<VertexConsumer> emitter) {
        BufferBuilder builder = acquireBuilder();
        long profileEmit = ClientRenderProfiler.begin();
        try {
            builder.begin(mode, format);
            emitter.accept(builder);
            BufferBuilder.RenderedBuffer rendered = builder.endOrDiscardIfEmpty();
            ClientRenderProfiler.end(ClientRenderProfiler.BAKE_EMIT, profileEmit);
            if (rendered == null) return new BakedMesh(null, 0, false);

            int bytes = rendered.drawState().bufferSize();
            boolean extendedFormat = rendered.drawState().format() != format;

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
            return new BakedMesh(vertexBuffer, bytes, extendedFormat);
        } finally {
            builderDepth--;
        }
    }

    private static BufferBuilder acquireBuilder() {
        int depth = builderDepth++;
        if (depth == BUILDERS.size()) BUILDERS.add(new BufferBuilder(BUILD_CAPACITY));
        return BUILDERS.get(depth);
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
