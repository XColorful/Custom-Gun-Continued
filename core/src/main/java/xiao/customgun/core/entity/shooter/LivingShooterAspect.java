/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import xiao.customgun.core.api.entity.ShooterProperty;

public abstract class LivingShooterAspect {

    protected final LivingEntity livingShooter;
    protected final ShooterProperty shooterProperty;

    public LivingShooterAspect(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        this.livingShooter = livingShooter;
        this.shooterProperty = shooterProperty;
    }
}
