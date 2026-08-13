/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.mixin.model;

import dev.xcolorful.customgun.client.CustomGunClient;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidModelMixin<T extends LivingEntity> {
    @Shadow
    @Final public ModelPart head;

    @Shadow
    @Final public ModelPart body;

    @Shadow
    @Final public ModelPart leftArm;

    @Shadow
    @Final public ModelPart rightArm;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "TAIL"))
    private void cgc$setRotationAnglesHead(T entityIn,
                                       float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
                                       CallbackInfo ci) {
        if (ageInTicks == 0) {
            return;
        }
        CustomGunClient.getShooterAnimationManager().setRotationAnglesHead(entityIn, head, body, leftArm, rightArm, limbSwingAmount);
    }
}
