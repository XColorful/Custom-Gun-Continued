/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.entity.ShooterProperty;

public abstract class LivingShooterAspect {
    @ApiStatus.Internal public static final int WINDOW_TIME_MS = 5;
    @ApiStatus.Internal public static final int NETWORK_DELAY_MS = 300;
    @ApiStatus.Internal public static final float CHARGE_PROGRESS_TOLERANCE = 0.001f;
    @ApiStatus.Internal public static final int CHARGE_TICK_TOLERANCE = 4;

    protected final LivingEntity livingShooter;
    protected final ShooterProperty shooterProperty;

    public LivingShooterAspect(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        this.livingShooter = livingShooter;
        this.shooterProperty = shooterProperty;
    }
}
