/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import xiao.customgun.client.api.entity.LocalShooterProperty;

public final class LocalShooterAim extends LocalShooterAspect {

    public LocalShooterAim(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void aim(boolean isAim) {
        // TODO
    }

    public float getClientAimingProgress(float partialTicks) {
        return Mth.lerp(partialTicks, this.localShooterProperty.oldAimingProgress, this.localShooterProperty.clientAimingProgress);
    }

    public boolean isAim() {
        return this.localShooterProperty.clientIsAiming;
    }

    public void tickAimingProgress() {
        // TODO
    }
}
