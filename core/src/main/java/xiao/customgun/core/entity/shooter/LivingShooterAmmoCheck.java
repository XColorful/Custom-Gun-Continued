/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.config.GunConfig;
import xiao.customgun.core.developer.PlannedRefactor;

public final class LivingShooterAmmoCheck extends LivingShooterAspect {

    public LivingShooterAmmoCheck(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public boolean needCheckAmmo() {
        if (PlannedRefactor.ON_CREATIVE_NO_AMMO_CHECK) return false;
        return !(this.livingShooter instanceof Player player) || !player.isCreative();
    }

    public boolean consumesAmmoOrNot() {
        if (this.livingShooter instanceof Player player) {
            return !player.isCreative() || GunConfig.CREATIVE_PLAYER_CONSUME_AMMO.get();
        }
        return true;
    }
}
