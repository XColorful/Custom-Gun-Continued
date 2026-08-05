/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.renderer.victim;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.ICustomEvent;
import dev.xcolorful.customgun.core.api.event.ICustomEventHandler;
import dev.xcolorful.customgun.core.api.event.projectile.ProjectileHitEntityEvent;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class GunHurtBobTweak implements ICustomEventHandler {
    private static class GunHurtBobTweakHolder {
        private static final GunHurtBobTweak INSTANCE = new GunHurtBobTweak();
    }
    public static GunHurtBobTweak get() {
        return GunHurtBobTweakHolder.INSTANCE;
    }
    protected GunHurtBobTweak() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(CustomEventType eventType, ICustomEvent event) {
        if (eventType == CustomEventType.PROJECTILE_HIT_ENTITY_EVENT) {
            onProjectileHit((ProjectileHitEntityEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    private void onProjectileHit(ProjectileHitEntityEvent event) {
        if (event.getLogicalSide().isServer()) return;

        @Nullable Entity victimEntity = event.getVictimEntity();
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer != null && localPlayer.equals(victimEntity)) {
            // 受伤的是自己 -> 触发受伤晃动的镜头调整
            var gunLocation = event.getGunLocation();
            @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
            if (gunIndexInstance == null) return;

            GunData gunData = gunIndexInstance.getGunData();
            float tweakMultiplier = gunData.getHurtBobTweakMultiplier();
            markTimestamp(tweakMultiplier);
        }
    }

    private static long hurtByGunTimeStamp = -1L;
    private static float lastTweakMultiplier = 0.05f;
    public static int VANILLA_HURT_TWEAK_MS = 500;

    public static boolean onHurtBobTweak(CameraRenderState cameraState, PoseStack matrixStack) {
        // 原版受伤的时长是 500 ms，所以如果大于 500 ms，那么说明不是子弹造成的伤害了
        if (System.currentTimeMillis() - hurtByGunTimeStamp > VANILLA_HURT_TWEAK_MS) {
            // 返回 false，让程序调用原版受伤晃动
            return false;
        }
        float zRot = cameraState.entityRenderState.hurtTime;
        if (zRot < 0) {
            return true;
        }
        float duration = (float) cameraState.entityRenderState.hurtDuration;
        if (duration <= 0) return true;
        zRot /= duration;
        zRot = Mth.sin(zRot * zRot * zRot * zRot * (float) Math.PI);
        float yRot = cameraState.entityRenderState.hurtDir;

        yRot = yRot * lastTweakMultiplier;
        zRot = zRot * lastTweakMultiplier;

        matrixStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        matrixStack.mulPose(Axis.XP.rotationDegrees(-zRot * 14.0F));
        matrixStack.mulPose(Axis.YP.rotationDegrees(yRot));
        return true;
    }

    public static void markTimestamp(float tweakMultiplier) {
        hurtByGunTimeStamp = System.currentTimeMillis();
        lastTweakMultiplier = tweakMultiplier;
    }
}
