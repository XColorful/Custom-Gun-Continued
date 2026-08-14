/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.event.render.ItemInHandBobEvent;
import dev.xcolorful.customgun.client.api.event.render.LevelBobEvent;
import dev.xcolorful.customgun.client.renderer.item.gun.GunCameraHelper;
import dev.xcolorful.customgun.client.renderer.victim.GunHurtBobTweak;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

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
        boolean cancel; {
            if (GunCameraHelper.State.renderItemInHand) {
                cancel = CustomGun.getEventPoster().postCustomEvent(new ItemInHandBobEvent.Hurt());
            } else {
                cancel = CustomGun.getEventPoster().postCustomEvent(new LevelBobEvent.Hurt());
            }
        }

        if (cancel) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    public void cgc$onBobView( // CameraRenderState cameraState
                              PoseStack poseStack,
                              float pPartialTicks, CallbackInfo ci) {
        boolean cancel; {
            if (GunCameraHelper.State.renderItemInHand) {
                cancel = CustomGun.getEventPoster().postCustomEvent(new ItemInHandBobEvent.View());
            } else {
                cancel = CustomGun.getEventPoster().postCustomEvent(new LevelBobEvent.View());
            }
        }

        if (cancel) {
            ci.cancel();
        }
    }

    /**
     * 是一个 hack 实现。
     * <ul>
     *     <li>因为 getFov 这个方法只有在构建 投影矩阵 的时候调用</li>
     *     <li>所以可以根据 getFov 中的 pUseFovSetting 来判断当前准备渲染 Level 还是渲染 HandWithItem </li>
     *     <li>至于为什么不直接对 renderItemInHand 这个方法 mixin ，是因为安装了 Optifine 之后，这个方法的内容被大幅度修改了</li>
     * </ul>
     */
    @Deprecated(since = "26.1.x")
    @Inject(method = "getFov", at = @At("HEAD"))
    public void cgc$switchRenderType(Camera pActiveRenderInfo,
                                     float pPartialTicks,
                                     boolean pUseFOVSetting,
                                     CallbackInfoReturnable<Double> cir) {
//        GunCameraHelper.State.renderItemInHand = !pUseFOVSetting;
    }
    /**
     * 楼上说得对，但是时代是会变的，咱别管optifine了
     */
    @Inject(method = "renderItemInHand", at = @At("HEAD"))
    private void cgc$beforeRenderItemInHand(PoseStack poseStack,
                                            Camera camera,
                                            float partialTick,
                                            CallbackInfo ci) {
        GunCameraHelper.State.renderItemInHand = true;
    }
    @Inject(method = "renderItemInHand", at = @At("RETURN"))
    private void cgc$afterRenderItemInHand(PoseStack poseStack,
                                           Camera camera,
                                           float partialTick,
                                           CallbackInfo ci) {
        GunCameraHelper.State.renderItemInHand = false;
    }
}
