/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.sound.SoundPlayManager;

public final class LocalShooterReload extends LocalShooterAspect {

    public LocalShooterReload(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void cancelReload() {
        // TODO
    }

    public void reload() {
        // TODO
    }

    private void doReload(GunDisplayInstance gunDisplayInstance) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        boolean noAmmo = false;
        SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(noAmmo ? GunSoundType.RELOAD_EMPTY_SOUND : GunSoundType.RELOAD_TACTICAL_SOUND),
                localPlayer);
    }
}
