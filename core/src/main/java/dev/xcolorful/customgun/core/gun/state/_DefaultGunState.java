/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gun.state;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.gun._HeatData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class _DefaultGunState {

    protected static void tickHeat(ShooterProperty shooterProperty,
                                   @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                   ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                   long heatTimestamp) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        if (heatData == null) return;

        if (!iGun.hasHeat(gunItem)) return;

        long currentTimeMillis = System.currentTimeMillis();
        if (iGun.hasOverheatLock(gunItem)) {
            // 过热锁
            if (currentTimeMillis - heatTimestamp < heatData.getOverheatLocktimeMs()) return;

            float heatAmount = iGun.getHeatCount(gunItem)
                    - ((float) (currentTimeMillis - heatTimestamp) / 10_000f)
                    * heatData.getCoolingSpeedMultiplier();
            iGun.setHeatCount(gunItem, heatAmount);

            // 解除过热锁
            if (heatAmount <= 0) {
                iGun.setOverheatLock(gunItem, false);
            }
        } else {
            // 降温
            if (currentTimeMillis - heatTimestamp < heatData.getCoolingDelayMs()) return;

            float heatAmount = iGun.getHeatCount(gunItem)
                    - ((float) (currentTimeMillis - heatTimestamp) / 10_000f)
                    * heatData.getCoolingSpeedMultiplier();

            iGun.setHeatCount(gunItem, heatAmount);
        }
    }
}
