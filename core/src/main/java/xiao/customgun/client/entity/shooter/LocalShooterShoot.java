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
import xiao.customgun.core.api.entity.ShootResult;
import xiao.customgun.core.config.GunConfig;
import xiao.customgun.core.developer.PlannedRefactor;
import xiao.customgun.core.resource.data.data.GunData;

public final class LocalShooterShoot extends LocalShooterAspect {

    public LocalShooterShoot(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public boolean chargeShoot(boolean isCharging) {
        // TODO
        return false;
    }

    public ShootResult shoot() {
        // TODO
        return ShootResult.SUCCESS;
    }

    private void preCheck(GunDisplayInstance gunDisplayInstance) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        var soundPlayManager = SoundPlayManager.get();
        if (soundPlayManager.isAllowDryFire()) soundPlayManager.playGunSound(gunDisplayInstance.getGunSound(GunSoundType.DRY_FIRE_SOUND),
                1.0f,
                localPlayer,
                GunConfig.DEFAULT_GUN_OTHER_SOUND_DISTANCE.get(),
                false);
    }

    private void doShoot(GunDisplayInstance gunDisplayInstance) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        GunData gunData = null;
        boolean useSilenceSound = false;
        if (PlannedRefactor.ON_MAGIC_CLIENT_SOUND_VOLUME) {}
        else SoundPlayManager.get().playGunSound(gunDisplayInstance.getGunSound(GunSoundType.SHOOT_SOUND),
                0.8f,
                localPlayer,
                GunConfig.DEFAULT_GUN_FIRE_SOUND_DISTANCE.get() * (useSilenceSound ? gunData.getFireSoundData().getSilencedMultiplier() : gunData.getFireSoundData().getNormalMultiplier()),
                false);
    }

    public long getClientShootCooldown() {
        // TODO
        return 0;
    }
}
