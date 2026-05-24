/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;

public final class LivingShooterReload extends LivingShooterAspect {

    private final LivingShooterDrawGun draw;
    private final LivingShooterShoot shoot;

    public LivingShooterReload(LivingEntity livingShooter, ShooterProperty shooterProperty,
                               LivingShooterDrawGun draw, LivingShooterShoot shoot) {
        super(livingShooter, shooterProperty);
        this.draw = draw;
        this.shoot = shoot;
    }

    public void reload() {
        if (this.shooterProperty.currentGunItem == null) return;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        // TODO
    }

    public void cancelReload() {
        if (this.shooterProperty.currentGunItem == null) return;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        // TODO
    }

    public ReloadState tickReloadState() {
        ReloadState result = new ReloadState();
        // 不在换弹
        if (this.shooterProperty.reloadTimestamp < 0) {
            return result;
        }

        // TODO
        return result;
    }
}
