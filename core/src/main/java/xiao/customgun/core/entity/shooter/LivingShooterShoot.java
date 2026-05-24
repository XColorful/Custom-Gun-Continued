/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.entity.ShootResult;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;

import java.util.function.Supplier;

public final class LivingShooterShoot extends LivingShooterAspect {

    private final LivingShooterDrawGun draw;

    public LivingShooterShoot(LivingEntity livingShooter, ShooterProperty shooterProperty,
                              LivingShooterDrawGun draw) {
        super(livingShooter, shooterProperty);
        this.draw = draw;
    }

    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp) {
        return shootInternal(pitch, yaw, timestamp, 0f, false);
    }
    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress) {
        return shootInternal(pitch, yaw, timestamp, chargeProgress, true);
    }
    private ShootResult shootInternal(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress, boolean hasChargeContext) {
        if (this.shooterProperty.currentGunItem == null) return ShootResult.NOT_DRAW;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return ShootResult.NOT_GUN;

        // TODO
        return ShootResult.SUCCESS;
    }

    /**
     * 以当前时间戳查询射击冷却。返回值一般不会超过枪械的射击间隔
     * @return 射击冷却
     */
    public long getShootCooldown() {
        return getShootCooldown(System.currentTimeMillis() - this.shooterProperty.baseTimestamp);
    }
    /**
     * 查询指定的 timestamp 下的射击冷却。根据情况返回值可能超过枪械的射击间隔。
     * @param timestamp 指定 timestamp，是偏移时间戳（基于base timestamp 的相对时间戳）
     * @return 射击冷却
     */
    public long getShootCooldown(long timestamp) {
        if (this.shooterProperty.currentGunItem == null) return 0;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return 0;

        // TODO
        return 0;
    }
}
