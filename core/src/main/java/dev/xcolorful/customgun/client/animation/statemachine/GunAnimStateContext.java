/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.statemachine;

import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.gun.script.GunScriptApi;
import dev.xcolorful.customgun.core.api.gun.script._LuaNbtAccessor;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterShoot;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.gun._HeatData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class GunAnimStateContext extends ItemAnimStateContext {

    private ItemStack currentGunItem;
    private IGun iGun;
    private GunDisplayInstance gunDisplayInstance;
    private GunData gunData;
    private float walkDistAnchor = 0f;
    private _LuaNbtAccessor nbtUtil;

    public GunAnimStateContext() {
    }

    // --------Deprecated--------
    // TODO 又一个"_GunScriptBackCompat"待移植
    // 做成单独的模组mixin？

    /**
     * @return {@link GunScriptApi#hasAmmoInBarrel()}
     */
    @Deprecated public boolean hasBulletInBarrel() {
        BoltType boltType = this.gunData.getBoltType();
        return boltType.useBarrelAmmo() && iGun.hasBarrelAmmo(this.currentGunItem);
    }
    /**
     * @return {@link GunScriptApi#isOverheatLocked()}
     */
    @Deprecated public boolean isOverHeat() {
        return iGun.hasOverheatLock(this.currentGunItem);
    }
    @Deprecated public float getHeatProgress() {
        _HeatData heatData = this.gunData.getHeatData();
        return heatData != null &&iGun.hasHeat(this.currentGunItem) ? iGun.getHeatCount(this.currentGunItem) / heatData.getMaxHeat() : 0;
    }

    /**
     * 获取枪械的射击间隔，单位毫秒
     * @return 射击间隔
     */
    @Deprecated public long getShootInterval() {
        Entity entity = Minecraft.getInstance().cameraEntity;
        if (!(entity instanceof LivingEntity livingEntity)) return 0;

        FireModeType fireModeType = iGun.getFireModeType(this.currentGunItem);
        return LivingShooterShoot._getShootInterval(livingEntity, this.gunData, fireModeType, iGun, this.currentGunItem);
    }
}
