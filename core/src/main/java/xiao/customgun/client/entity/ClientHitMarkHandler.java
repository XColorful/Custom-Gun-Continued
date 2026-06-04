/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.sound.SoundPlayManager;

/**
 * 原 ClientHitMark
 */
public class ClientHitMarkHandler {

    public static void onEntityHurt() {
        GunDisplayInstance gunDisplayInstance = null;
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        boolean headshot = false;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(headshot ? GunSoundType.HEAD_HIT_SOUND : GunSoundType.BOLT_SOUND.FLESH_HIT_SOUND),
                localPlayer);
    }

    public static void onEntityKill() {
        GunDisplayInstance gunDisplayInstance = null;
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.KILL_SOUND),
                localPlayer);
    }
}
