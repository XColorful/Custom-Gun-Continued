/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.core.entity.shooter.LivingShooterAspect;

public abstract class LocalShooterAspect {
    @ApiStatus.Internal public static final int RELOAD_COOLDOWN_MS = LivingShooterAspect.RELOAD_COOLDOWN_MS + 50;
    @ApiStatus.Internal public static final int SHOOT_COOLDOWN_MS = 50;

    protected final LocalPlayer localShooter;
    protected final LocalShooterProperty localShooterProperty;

    public LocalShooterAspect(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        this.localShooter = localShooter;
        this.localShooterProperty = localShooterProperty;
    }
}
