/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter.modifier;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.api.event.shooter.ShooterGunModifierCacheEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

public class ShooterGunModifierManager {

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
        ShooterGunModifierCache gunPropertyCache = ShooterGunModifierCache.of(gunIndexInstance, iGun, gunItem);

        CustomGun.getEventPoster().postCustomEvent(new ShooterGunModifierCacheEvent(CustomGun.getSideExecutor().getLogicalSide(),
                iLivingShooter, livingShooter,
                iGun, gunItem,
                gunPropertyCache));

        {
            ShooterProperty shooterProperty = iLivingShooter.cgc$getShooterProperty();
            // TODO 是否可被脚本修改，写到IGunModifier default空实现，并让IGunModifier同包子接口提供default重载
        }

        // 写入缓存
        iLivingShooter.cgc$updateGunModifierCache(gunPropertyCache);
    }
}
