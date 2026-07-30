/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.action;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

@ApiStatus.Internal
public class _DefaultGunAction {

    /**
     * 限定{@link BoltType#MANUAL_ACTION}
     */
    protected static boolean startBolt(ShooterProperty shooterProperty,
                                      @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                      ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        if (iGun.hasBarrelAmmo(gunItem)) { // 枪管有子弹则不拉栓
            return false;
        }

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return false;

        // 检查 bolt 类型是否是 manual action
        BoltType boltType = gunIndexInstance.getGunData().getBoltType();
        if (boltType != BoltType.MANUAL_ACTION) return false;

        // 检查是否有子弹可拉栓
        boolean hasAmmo = iGun.useInventoryAmmo(gunItem)
                ? iGun.hasInventoryAmmo(livingShooter, gunItem) // 背包直读
                : iGun.getMagAmmoCount(gunItem) < 1;
        if (!hasAmmo) return false;

        return true;
    }

    /**
     * {@link _DefaultGunAction#startBolt}已经限定了{@link BoltType#MANUAL_ACTION}
     * @return 是否还在拉栓
     */
    protected static boolean tickBolt(ShooterProperty shooterProperty,
                                      @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                      ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        float rawBoltFeedTime = gunData.getBoltFeedTime();
        long boltActionTimeMs = (long) (gunData.getBoltActionTime() * 1000);
        long boltFeedTime = rawBoltFeedTime <= 0 ? boltActionTimeMs : (long) (rawBoltFeedTime * 1000);

        float boltTime = shooterProperty.isBolting ? System.currentTimeMillis() - shooterProperty.boltTimestamp : 0;
        if (boltTime < boltFeedTime) { // 没到上弹时间
            return true;
        }

        BoltType boltType = gunData.getBoltType();
        if (boltType == BoltType.OPEN_BOLT) return false; // open bolt不需要拉栓

        // 仅在枪管没子弹才上弹 (一次bolt只触发一次)
        if (!iGun.hasBarrelAmmo(gunItem)) {
            int consumedAmmo = iGun.useInventoryAmmo(gunItem)
                    ? iGun.consumeAmmoOnce(livingShooter, gunItem) // 背包直读
                    : iGun.consumeMagAmmo(gunItem); // 枪械弹匣供弹 (要先装弹)
            if (consumedAmmo > 0) {
                // 上弹到枪管
                iGun.setBarrelAmmoCount(gunItem, consumedAmmo);
            }
        }

        if (boltTime >= gunData.getBoltActionTime()) { // 完成bolt
            return false;
        }

        return true;
    }

    protected static boolean startReload(ShooterProperty shooterProperty,
                                         @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                         ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        // TODO
        return true;
    }

    protected static ReloadState tickReload(ShooterProperty shooterProperty,
                                            @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                            ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        // TODO
        return new ReloadState();
    }

    protected static void interruptReload(ShooterProperty shooterProperty,
                                          @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                          ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        // TODO
    }
}
