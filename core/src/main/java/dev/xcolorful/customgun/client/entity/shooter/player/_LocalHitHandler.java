/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.entity.shooter.player;

import dev.xcolorful.customgun.client.CustomGunClient;
import dev.xcolorful.customgun.client.api.gui.overlay.IOverlayManager;
import dev.xcolorful.customgun.client.api.gui.overlay.OverlayStateAccessor;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.sound.gun.GunSoundType;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.sound.SoundPlayManager;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.ICustomEvent;
import dev.xcolorful.customgun.core.api.event.ICustomEventHandler;
import dev.xcolorful.customgun.core.api.event.projectile.ProjectileHitEntityFinishEvent;
import dev.xcolorful.customgun.core.api.event.projectile.ProjectileKillEntityEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * 原 ClientHitMark
 */
public class _LocalHitHandler implements ICustomEventHandler {
    private static class _LocalHitHandlerHolder {
        private static final _LocalHitHandler INSTANCE = new _LocalHitHandler();
    }
    public static _LocalHitHandler get() {
        return _LocalHitHandlerHolder.INSTANCE;
    }
    protected _LocalHitHandler() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(CustomEventType eventType, ICustomEvent event) {
        switch (eventType) {
            case PROJECTILE_HIT_ENTITY_FINISH_EVENT -> {
                onProjectileHitEntityFinish((ProjectileHitEntityFinishEvent) event);
            }
            case PROJECTILE_KILL_ENTITY_EVENT -> {
                onProjectileKillEntity((ProjectileKillEntityEvent) event);
            }
            default -> {
                onReceiveWrongEvent(eventType);
            }
        }
    }

    public static void onProjectileHitEntityFinish(ProjectileHitEntityFinishEvent event) {
        if (event.getLogicalSide().isServer()) return;

        @Nullable Entity shooter = event.context.getCausingEntity();
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || localPlayer != shooter) return;

        boolean isHeadshot = event.context.isHeadshot();
        { // 标记overlay信息
            long currentTimeMillis = System.currentTimeMillis();
            IOverlayManager overlayManager = CustomGunClient.getOverlayManager();
            OverlayStateAccessor state = overlayManager.getState();

            state.setHitTimestamp(currentTimeMillis);
            if (isHeadshot) state.setHeadshotTimestamp(currentTimeMillis);
        }

        @Nullable GunDisplayInstance gunDisplayInstance; {
            var gunLocation = event.getGunLocation();
            var gunDisplayLocation = event.getDisplayLocation();
            gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunDisplayLocation, gunLocation);
        }
        if (gunDisplayInstance == null) return;

        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(isHeadshot ? GunSoundType.HEAD_HIT_SOUND : GunSoundType.FLESH_HIT_SOUND),
                localPlayer);
    }

    public static void onProjectileKillEntity(ProjectileKillEntityEvent event) {
        if (event.getLogicalSide().isServer()) return;

        @Nullable Entity shooter = event.context.getCausingEntity();
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || localPlayer != shooter) return;

        { // 标记overlay信息
            long currentTimeMillis = System.currentTimeMillis();
            IOverlayManager overlayManager = CustomGunClient.getOverlayManager();
            OverlayStateAccessor state = overlayManager.getState();

            state.setKillTimestamp(currentTimeMillis);
        }

        @Nullable GunDisplayInstance gunDisplayInstance; {
            var gunLocation = event.getGunLocation();
            var gunDisplayLocation = event.getDisplayLocation();
            gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunDisplayLocation, gunLocation);
        }
        if (gunDisplayInstance == null) return;

        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.KILL_SOUND),
                localPlayer);
    }
}
