package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xcolorful.customgun.client.model.bake.BakedDrawSink;
import net.minecraft.client.renderer.StagedVertexBuffer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.2 延迟渲染的静态优化（渲染重构B1-a）：外部缓冲的 draw 在绘制阶段改报自己的缓冲
 * <ul>
 *     <li>原版对空 draw 返回 {@code null}（不画）；这里在判空之前接管，索引走原版的顺序索引缓冲</li>
 *     <li>索引口径与非排序绘制相同：{@code PrimitiveTopology#indexCount(vertexCount)}</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.2")
@Mixin(StagedVertexBuffer.class)
public class StagedVertexBufferMixin {

    @Inject(method = "getExecuteInfo", at = @At("HEAD"), cancellable = true)
    private void cgc$externalExecuteInfo(StagedVertexBuffer.Draw draw,
                                         CallbackInfoReturnable<StagedVertexBuffer.@Nullable ExecuteInfo> cir) {
        if (!(draw instanceof BakedDrawSink.ExternalDraw external)) return;

        @Nullable GpuBuffer vertexBuffer = external.cgc$vertexBuffer();
        @Nullable PrimitiveTopology topology = external.cgc$topology();
        if (vertexBuffer == null || topology == null) return;

        int indexCount = external.cgc$indexCount();
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(topology);
        StagedVertexBuffer.ExecuteInfo info = new StagedVertexBuffer.ExecuteInfo(
                vertexBuffer, indices.getBuffer(indexCount), indices.type(), 0, 0, indexCount);
        // 绘制紧接着发生：把这次绘制的实例姿态挂上去，绘制期间补进模型视图栈（光影包的法线矩阵取这里）
        @Nullable Matrix4f pose = external.cgc$pose();
        if (pose != null) BakedDrawSink.setPendingDraw(info, pose);
        cir.setReturnValue(info);
    }
}
