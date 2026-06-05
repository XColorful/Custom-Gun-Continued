/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import xiao.customgun.client.api.entity.LocalShooterProperty;

public abstract class LocalShooterAspect {

    protected final LocalPlayer localShooter;
    protected final LocalShooterProperty localShooterProperty;

    public LocalShooterAspect(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        this.localShooter = localShooter;
        this.localShooterProperty = localShooterProperty;
    }
}
