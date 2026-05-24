/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;

public final class LivingShooterMelee extends LivingShooterAspect {

    public LivingShooterMelee(LivingEntity livingShooter, ShooterProperty shooterProperty,
                              LivingShooterDrawGun drawGun) {
        super(livingShooter, shooterProperty);
    }

    public void melee() {
        if (this.shooterProperty.currentGunItem == null) return;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        // TODO
    }

    public void scheduleTickMelee() {
        // TODO
    }

    public long getMeleeCooldown() {
        if (this.shooterProperty.currentGunItem == null) return 0;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return 0;

        // TODO
        return 0;
    }
}
