/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.sound.SoundPlayManager;

public class LocalShooterInspect extends LocalShooterAspect {

    public void inspect() {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        boolean noAmmo = false;
        GunDisplayInstance gunDisplayInstance = null;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(noAmmo ? GunSoundType.INSPECT_EMPTY_SOUND : GunSoundType.INSPECT_SOUND),
                localPlayer);
    }
}
