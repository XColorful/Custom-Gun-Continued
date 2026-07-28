/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.mixin.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.client.api.renderer.KeepingItemRenderer;

@Mixin(PlayerModel.class)
public class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {
    @Shadow
    @Final public ModelPart leftSleeve;

    @Shadow
    @Final public ModelPart rightSleeve;

    public PlayerModelMixin(ModelPart part) {
        super(part);
    }

    /**
     * 用于清除默认的手臂旋转
     */
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At(value = "TAIL"))
    private void cgc$setRotationAnglesTail(AvatarRenderState renderState,
                                       CallbackInfo ci) {
//        if (!(entityIn instanceof Player player)) {
//            return;
//        }

        // 用于清除默认的手臂旋转
        ItemStack currentItem = KeepingItemRenderer.cgc$getRenderer().cgc$getCurrentItem();
//        if (ageInTicks == 0F // 第一人称渲染时
//                && IGunGetter.fromItemStack(currentItem) != null) {
//            cgc$resetRotation(this.rightArm);
//            cgc$resetRotation(this.leftArm);
//            this.rightSleeve.copyFrom(this.rightArm);
//            this.leftSleeve.copyFrom(this.leftArm);
//        }
    }

    /**
     * 将给定模型的旋转角度和旋转点重置为零
     */
    private void cgc$resetRotation(ModelPart part) {
        part.xRot = 0.0F;
        part.yRot = 0.0F;
        part.zRot = 0.0F;
    }
}
