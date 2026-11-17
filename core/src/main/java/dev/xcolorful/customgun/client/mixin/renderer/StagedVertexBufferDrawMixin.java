package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import dev.xcolorful.customgun.client.model.bake.BakedDrawSink;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.joml.Matrix4f;

/**
 * 26.2 延迟渲染的静态优化（渲染重构B1-a）：给一条 draw 挂上外部顶点缓冲
 * <ul>
 *     <li>这类 draw 的顶点不进 {@code StagedVertexBuffer} 的暂存区，上传阶段按空 draw 跳过</li>
 *     <li>绘制阶段由 {@code StagedVertexBufferMixin} 改报外部缓冲与其索引数</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.2")
@Mixin(targets = "net.minecraft.client.renderer.StagedVertexBuffer$Draw")
public class StagedVertexBufferDrawMixin implements BakedDrawSink.ExternalDraw {

    @Unique
    private @Nullable GpuBuffer cgc$vertexBuffer;
    @Unique
    private int cgc$indexCount;
    @Unique
    private @Nullable PrimitiveTopology cgc$topology;
    @Unique
    private @Nullable Matrix4f cgc$pose;

    @Override
    public void cgc$setExternal(GpuBuffer vertexBuffer, int indexCount, PrimitiveTopology topology, Matrix4f pose) {
        this.cgc$vertexBuffer = vertexBuffer;
        this.cgc$indexCount = indexCount;
        this.cgc$topology = topology;
        this.cgc$pose = pose;
    }

    @Override
    public @Nullable GpuBuffer cgc$vertexBuffer() {
        return this.cgc$vertexBuffer;
    }

    @Override
    public int cgc$indexCount() {
        return this.cgc$indexCount;
    }

    @Override
    public @Nullable PrimitiveTopology cgc$topology() {
        return this.cgc$topology;
    }

    @Override
    public @Nullable Matrix4f cgc$pose() {
        return this.cgc$pose;
    }
}
