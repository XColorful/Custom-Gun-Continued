package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.client.model.bake.StaticBatch;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 跨实例合批的帧边界（渲染重构B2）
 * <ul>
 *     <li>实体循环里只登记实例，绘制统一放到循环之后：此时相机与雾都在世界阶段的状态里，且所有实例的世界矩阵都已知</li>
 *     <li>第一个 {@code checkPoseStack} 调用紧跟实体循环的 {@code endLastBatch()}，是 1.20.1 里唯一可锚定的位置</li>
 * </ul>
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void cgc$beginStaticBatch(PoseStack poseStack,
                                      float partialTick,
                                      long finishTimeNano,
                                      boolean renderBlockOutline,
                                      Camera camera,
                                      GameRenderer gameRenderer,
                                      LightTexture lightTexture,
                                      Matrix4f projectionMatrix,
                                      CallbackInfo ci) {
        StaticBatch.beginFrame();
    }

    @Inject(method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER))
    private void cgc$flushStaticBatch(PoseStack poseStack,
                                      float partialTick,
                                      long finishTimeNano,
                                      boolean renderBlockOutline,
                                      Camera camera,
                                      GameRenderer gameRenderer,
                                      LightTexture lightTexture,
                                      Matrix4f projectionMatrix,
                                      CallbackInfo ci) {
        StaticBatch.flush();
    }
}
