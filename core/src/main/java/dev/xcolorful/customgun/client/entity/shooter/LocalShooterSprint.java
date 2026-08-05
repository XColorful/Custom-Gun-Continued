/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.entity.shooter;

import dev.xcolorful.customgun.client.api.entity.LocalShooterProperty;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterSprint;
import net.minecraft.client.player.LocalPlayer;

public final class LocalShooterSprint extends LocalShooterAspect {

    /**
     * 是否强制取消疾跑，目前是纯客户端拦截
     */
    public static boolean forceDisableSprint = false; // TODO 移除static

    public LocalShooterSprint(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public boolean onSprint(boolean isSprint) {
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        if ( // 2.2 检查状态
            // 禁止疾跑的状态
                _shouldForceDisableSprint(iLivingShooter, isSprint)
        ) return false;

        return isSprint;
    }
    /**
     * 对应{@link LivingShooterSprint#_shouldForceDisableSprint}
     */
    private boolean _shouldForceDisableSprint(ILivingShooter iLivingShooter, boolean isSprint) {
        if (LivingShooterSprint.isIllegalSprintState(iLivingShooter)) return true;

        if (forceDisableSprint) return true;

        return false;
    }
}
