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
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.client.renderer.victim.GunHurtBobTweak;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    private boolean cgc$useFovSetting;

    @Shadow
    public abstract Minecraft getMinecraft();

    @Shadow
    public abstract void render(float pPartialTicks, long pNanoTime,
                                boolean pRenderLevel);

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    public void cgc$onBobHurt( // CameraRenderState cameraState
                          PoseStack poseStack,
                          float pPartialTicks, CallbackInfo ci) {
        // 取消受伤导致的视角摇晃
        if (this.getMinecraft().getCameraEntity() instanceof LocalPlayer player && !player.isDeadOrDying()) {
            if (GunHurtBobTweak.onHurtBobTweak(player, poseStack, pPartialTicks)) {
                ci.cancel();
                return;
            }
        }
        // 触发其他事件
        boolean cancel = false;
        if (!cgc$useFovSetting) {
            // TODO RenderItemInHandBobEvent
        } else {
            // TODO RenderLevelBobEvent
        }
        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void cgc$onBobView( // CameraRenderState cameraState
                          PoseStack poseStack,
                          float pPartialTicks, CallbackInfo ci) {
        boolean cancel = false;
        if (!cgc$useFovSetting) {
            // TODO RenderItemInHandBobEvent
        } else {
            // TODO RenderLevelBobEvent
        }
        if (cancel) {
            ci.cancel();
        }
    }
}
