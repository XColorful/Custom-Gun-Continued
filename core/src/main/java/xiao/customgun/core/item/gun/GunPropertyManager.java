/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.gun;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.entity.shooter.ShooterGunPropertyCache;
import xiao.customgun.core.api.event.shooter.ShooterGunPropertyCacheEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

public class GunPropertyManager {

    public static void postChangeEvent(LivingEntity livingShooter) {
        postChangeEvent(livingShooter, livingShooter.getMainHandItem());
    }
    public static void postChangeEvent(@NotNull LivingEntity livingShooter, @NotNull ItemStack gunItem) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;

        // 更新缓存逻辑 (重新计算缓存值)
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(livingShooter);
        ShooterGunPropertyCache gunPropertyCache = updateShooterGunPropertyCache(gunIndexInstance, iGun, gunItem);

        CustomGun.getEventPoster().postCustomEvent(new ShooterGunPropertyCacheEvent(CustomGun.getSideExecutor().getLogicalSide(),
                iLivingShooter, livingShooter,
                iGun, gunItem,
                gunPropertyCache));

        {
            ShooterProperty shooterProperty = iLivingShooter.cgc$getShooterProperty();
            // TODO GunProperties移植 (待定, 目前迁移映射里为 xiao.customgun.core.api.projectile.IProjectileRuntime.StateCache.*)
        }

        // 写入缓存
        iLivingShooter.cgc$updateGunPropertyCache(gunPropertyCache);
    }
    /**
     * 计算缓存值
     */
    private static ShooterGunPropertyCache updateShooterGunPropertyCache(@NotNull GunIndexInstance gunIndexInstance,
                                                                         @NotNull IGun iGun, @NotNull ItemStack gunItem) {
        ShooterGunPropertyCache gunPropertyCache = new ShooterGunPropertyCache();
        GunData gunData = gunIndexInstance.getGunData();
        // TODO 原 ChangeGunPropertyEvent
        return gunPropertyCache;
    }
}
