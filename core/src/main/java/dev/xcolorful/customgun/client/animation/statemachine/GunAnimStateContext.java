/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.statemachine;

import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.gun.script.GunScriptApi;
import dev.xcolorful.customgun.core.api.gun.script._LuaNbtAccessor;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterShoot;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.gun._HeatData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class GunAnimStateContext extends ItemAnimStateContext {

    private ItemStack currentGunItem;
    private @Nullable IGun iGun;
    private GunDisplayInstance gunDisplayInstance;
    private GunData gunData;
    private float walkDistAnchor = 0f;
    private _LuaNbtAccessor nbtUtil;

    public GunAnimStateContext() {
    }

    // --------Setter--------

    /**
     * 状态机脚本请不要调用此方法。此方法用于状态机更新时设置当前的物品对象
     */
    @ApiStatus.Internal
    public void setCurrentGunItem(ItemStack gunItem) {
        this.currentGunItem = gunItem;
        this.iGun = IGunGetter.fromItemStack(gunItem);

        if (this.iGun != null) {
            @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
            this.gunDisplayInstance = gunDisplayInstance;

            var gunLocation = this.iGun.getGunLocation(gunItem);
            @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
            this.gunData = gunIndexInstance != null ? gunIndexInstance.getGunData() : null;
        }

        this.nbtUtil = _LuaNbtAccessor.of(this.currentGunItem);
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
