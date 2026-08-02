/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.developer.PlannedRefactor;

public final class LivingShooterSprint extends LivingShooterAspect {

    public LivingShooterSprint(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    /**
     * 根据情况返回玩家应当处于的冲刺状态
     * @param isSprint 当前冲刺状态
     * @return 当前是否应该处于冲刺状态
     */
    public boolean onSprint(boolean isSprint) {
        ILivingShooter livingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        if ( // 2.2 检查状态
                // 禁止疾跑的状态
                _shouldForceDisableSprint(livingShooter, isSprint)
        ) return false;

        return isSprint;
    }
    @ApiStatus.Internal
    public boolean _shouldForceDisableSprint(ILivingShooter iLivingShooter, boolean isSprint) {
        if (isIllegalSprintState(iLivingShooter)) return true;

        return false;
    }
    /**
     * 用于排除一定不允许冲刺的情况
     * @return 是否不可能(是否禁止)处于冲刺状态
     */
    @ApiStatus.Internal
    public static boolean isIllegalSprintState(ILivingShooter iLivingShooter) {
        boolean isAiming = iLivingShooter.cgc$getSynIsAiming();
        if (isAiming && !PlannedRefactor.SPRINT_ON_AIMING) return true;

        ReloadState.StateType reloadStateType = iLivingShooter.cgc$getSynReloadState().getStateType();
        if (!PlannedRefactor.SPRINT_ON_RELOADING && reloadStateType.isReloading() && !reloadStateType.isReloadFinishing()) return true;

        return false;
    }
}
