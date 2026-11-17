package dev.xcolorful.customgun.client.mixin.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.xcolorful.customgun.client.model.bake.BakedDrawSink;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 26.2 延迟渲染的静态优化（渲染重构B1-a）：外部缓冲绘制期间把实例姿态补进模型视图栈
 * <ul>
 *     <li>位置由 {@code DynamicTransforms}（模型视图 × 姿态）承担，不需要这一层</li>
 *     <li>光影包的 {@code iris_NormalMat} 取自绘制时的 {@link RenderSystem#getModelViewMatrixCopy()}，
 *         不补的话烘焙块的法线少一次姿态旋转（1.21.11 同一处修复，2026-11-16）</li>
 *     <li>无光影时没人读这个栈，推入 / 弹出没有副作用</li>
 * </ul>
 */
@ApiStatus.AvailableSince("26.2")
@Mixin(RenderTypeFeatureRenderer.class)
public class RenderTypeFeatureRendererDrawMixin {

    @WrapOperation(method = "executeGroup",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/rendertype/PreparedRenderType;drawFromBuffer(Lnet/minecraft/client/renderer/StagedVertexBuffer$ExecuteInfo;)V"))
    private void cgc$drawWithBakedPose(PreparedRenderType prepared,
                                       StagedVertexBuffer.ExecuteInfo info,
                                       Operation<Void> original) {
        @Nullable Matrix4f pose = BakedDrawSink.takePendingPose(info);
        if (pose == null) {
            original.call(prepared, info);
            return;
        }

        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.mul(pose);
        try {
            original.call(prepared, info);
        } finally {
            modelViewStack.popMatrix();
        }
    }
}
