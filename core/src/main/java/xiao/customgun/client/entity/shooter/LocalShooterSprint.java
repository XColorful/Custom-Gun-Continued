/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.entity.shooter.LivingShooterSprint;

public final class LocalShooterSprint extends LocalShooterAspect {

    public static boolean stopSprint = false;

    public LocalShooterSprint(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    /**
     * 根据情况返回玩家应当处于的冲刺状态，在玩家切换冲刺状态的时候调用
     * 这里的逻辑应该严格与服务端对应 {@link LivingShooterSprint#getProcessedSprintStatus}
     */
    public boolean getProcessedSprintStatus(boolean sprinting) {
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        ReloadState.StateType reloadStateType = iLivingShooter.cgc$getSynReloadState().getStateType();
        if (iLivingShooter.cgc$getSynIsAiming()
                || (reloadStateType.isReloading() && !reloadStateType.isReloadFinishing())
                || stopSprint
        ) return false;

        return sprinting;
    }
}
