/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.developer.PlannedRefactor;

public final class LivingShooterSprint extends LivingShooterAspect {

    public LivingShooterSprint(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public boolean getProcessedSprintStatus(boolean sprint) {
        ILivingShooter livingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        boolean isAiming = livingShooter.cgc$getSynIsAiming();
        if (isAiming && !PlannedRefactor.SPRINT_ON_AIMING) return false;

        ReloadState.StateType reloadStateType = livingShooter.cgc$getSynReloadState().getStateType();
        if (!PlannedRefactor.SPRINT_ON_RELOADING && reloadStateType.isReloading() && !reloadStateType.isReloadFinishing()) {
            return false;
        } else {
            return sprint;
        }
    }
}
