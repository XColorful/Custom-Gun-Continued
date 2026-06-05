/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.mixin.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.client.renderer.model.MuzzleFlashRender;
import xiao.customgun.client.renderer.model.ShellRender;
import xiao.customgun.client.renderer.shooter.HumanoidOffhandRender;
import xiao.customgun.core.api.item.gun.IGunGetter;

@Mixin(ItemInHandLayer.class)
public class ItemInHandLayerMixin {

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "TAIL"))
    private void render(PoseStack poseStack,
                        MultiBufferSource buffer,
                        int lightCoords,
                        LivingEntity livingEntity, float limbSwing, float limbSwingAmount,
                        float partialTicks, float ageInTicks,
                        float pNetHeadYaw, float pHeadPitch,
                        CallbackInfo ci) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
        HumanoidOffhandRender.renderGun(livingEntity, poseStack, buffer, lightCoords);
    }

    @Inject(method = "renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "HEAD"), cancellable = true)
    private void renderArmWithItemHead(LivingEntity livingEntity,
                                       ItemStack itemStack, ItemDisplayContext pDisplayContext,
                                       HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                       CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (livingEntity.equals(player)) {
            MuzzleFlashRender.isSelf = true;
            ShellRender.isSelf = true;
        }
        if (IGunGetter.fromMainHand(livingEntity) != null && arm == HumanoidArm.LEFT) {
            ci.cancel();
        }
    }

    @Inject(method = "renderArmWithItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "TAIL"))
    private void renderArmWithItemTail(LivingEntity livingEntity,
                                       ItemStack itemStack,
                                       ItemDisplayContext pDisplayContext,
                                       HumanoidArm arm, PoseStack poseStack,
                                       MultiBufferSource buffer,
                                       int lightCoords, CallbackInfo ci) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
    }
}
