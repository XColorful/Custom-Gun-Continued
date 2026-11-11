package dev.xcolorful.customgun.client.model.bake;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.function.Consumer;

/**
 * 模型空间的静态烘焙网格
 * <ul>
 *     <li>顶点只生成一次，常驻 {@link VertexBuffer}；每帧只绑定并换模型视图矩阵绘制</li>
 *     <li>实例矩阵不进顶点，因此同一份网格可以被多个展示框 / 掉落物共享</li>
 *     <li>只支持即时模式：{@code RenderType} 的 setup/clear 包住 draw，与 {@code BufferSource.endBatch} 的做法一致</li>
 * </ul>
 * 失效条件由调用方放进 {@link StaticBakeCache.Key}，本类不判断。
 */
@ApiStatus.Internal
public final class BakedMesh implements AutoCloseable {

    /**
     * 单次烘焙的初始顶点缓冲容量；超出时 {@link BufferBuilder} 自行扩容
     */
    private static final int BUILD_CAPACITY = 1024 * 1024;

    private final @Nullable VertexBuffer vertexBuffer;
    private final int bytes;
    /**
     * 本次烘焙实际写入的是不是扩展顶点格式（光影包会把 {@code NEW_ENTITY} 换成
     * {@code IrisVertexFormats.ENTITY}）。
     * <p>
     * 绘制前要和当前光影状态比对：两者不一致说明光影包刚刚切换、缓存里的顶点布局已经不能用了，
     * 本帧必须退回即时管线。
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
        BufferBuilder builder = new BufferBuilder(BUILD_CAPACITY);
        builder.begin(mode, format);
        emitter.accept(builder);

        BufferBuilder.RenderedBuffer rendered = builder.endOrDiscardIfEmpty();
        if (rendered == null) return new BakedMesh(null, 0, false);

        int bytes = rendered.drawState().bufferSize();
        boolean extendedFormat = rendered.drawState().format() != format;
        VertexBuffer vertexBuffer = new VertexBuffer(VertexBuffer.Usage.STATIC);
        // VertexBuffer.upload 要求 VAO 已绑定，原版 BufferUploader 同样先 bind 再 upload
        vertexBuffer.bind();
        vertexBuffer.upload(rendered);
        VertexBuffer.unbind();
        return new BakedMesh(vertexBuffer, bytes, extendedFormat);
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
