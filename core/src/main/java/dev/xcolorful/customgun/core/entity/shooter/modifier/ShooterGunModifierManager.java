/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity.shooter.modifier;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterGunModifierCacheEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO 这个类现在跟原模组的AllowAttachmentTagMatcher一样是双端污染的
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

        McLogicalSide logicalSide = CustomGun.getSideExecutor().getLogicalSide();
        CustomGun.getEventPoster().postCustomEvent(new ShooterGunModifierCacheEvent(logicalSide,
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
