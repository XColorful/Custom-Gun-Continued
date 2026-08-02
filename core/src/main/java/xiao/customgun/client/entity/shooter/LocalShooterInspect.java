/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.sound.gun.GunSoundType;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.client.sound.SoundPlayManager;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

public final class LocalShooterInspect extends LocalShooterAspect {

    public LocalShooterInspect(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void inspect() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            // TODO AnimateGeoItemRenderer
            return;
        }

        // 检查状态锁
        if (this.localShooterProperty.clientStateLock) return;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;
        GunData gunData = gunIndexInstance.getGunData();

        @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
        if (gunDisplayInstance == null) return;

        BoltType boltType = gunData.getBoltType();
        boolean hasAmmo = boltType == BoltType.OPEN_BOLT ? iGun.getMagAmmoCount(gunItem) > 0
                : iGun.hasBarrelAmmo(gunItem);

        // 触发 inspect，停止播放声音
        SoundPlayManager.get().stopCurrentSound();
        var soundLocation = gunDisplayInstance.getGunSound(!hasAmmo ? GunSoundType.INSPECT_EMPTY_SOUND : GunSoundType.INSPECT_SOUND);
        SoundPlayManager.get().playGunSound(soundLocation,
                this.localShooter);
        // TODO GunDisplayInstance AnimationStateMachine
    }
}
