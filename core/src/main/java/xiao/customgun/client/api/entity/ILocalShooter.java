/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.entity;

import xiao.customgun.client.api.entity.shooter.IClientGunOperator;
import xiao.customgun.client.api.entity.shooter.ILocalShooterState;
import xiao.customgun.core.api.entity.ILivingShooter;

public interface ILocalShooter extends IClientGunOperator, ILocalShooterState {

    /**
     * 该方法命名与 {@link ILivingShooter#cgc$getShooterProperty()} 做出显式差异
     * (黑魔法)JVM字节码层面允许出现返回值不同的同名函数，mixin处理LocalPlayer才不会出问题
     */
    LocalShooterProperty cgc$getLocalShooterProperty();
}
