/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.mixin.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.HumanoidArm;
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
public class ItemInHandLayerMixin<S extends ArmedEntityRenderState, M extends EntityModel<S> & ArmedModel<?>> {

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;FF)V",
            at = @At(value = "TAIL"))
    private void cgc$render(PoseStack poseStack,
                        SubmitNodeCollector submitNodeCollector,
                        int lightCoords,
                        S renderState,
                        float partialTicks, float ageInTicks,
                        CallbackInfo ci) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
        HumanoidOffhandRender.renderGun(renderState, poseStack, submitNodeCollector, lightCoords);
    }

    @Inject(method = "submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At(value = "HEAD"), cancellable = true)
    private void cgc$renderArmWithItemHead(S renderState,
                                       ItemStackRenderState itemStackRenderState,
                                       ItemStack itemStack,
                                       HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int packedLight,
                                       CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
//        if (livingEntity.equals(player)) {
//            MuzzleFlashRender.isSelf = true;
//            ShellRender.isSelf = true;
//        }
        if (IGunGetter.fromItemStack(itemStack) != null && arm == HumanoidArm.LEFT) {
            ci.cancel();
        }
    }

    @Inject(method = "submitArmWithItem(Lnet/minecraft/client/renderer/entity/state/ArmedEntityRenderState;Lnet/minecraft/client/renderer/item/ItemStackRenderState;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/HumanoidArm;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V",
            at = @At(value = "TAIL"))
    private void cgc$renderArmWithItemTail(S renderState,
                                       ItemStackRenderState itemStackRenderState,
                                       ItemStack itemStack,
                                       HumanoidArm arm, PoseStack poseStack,
                                       SubmitNodeCollector submitNodeCollector,
                                       int lightCoords, CallbackInfo ci) {
        MuzzleFlashRender.isSelf = false;
        ShellRender.isSelf = false;
    }
}
