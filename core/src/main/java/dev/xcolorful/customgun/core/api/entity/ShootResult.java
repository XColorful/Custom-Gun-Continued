/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.entity;

import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackRuntime;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterShoot;
import dev.xcolorful.customgun.core.gun.attack._DefaultGunAttack;
import org.jetbrains.annotations.ApiStatus;

public enum ShootResult {
    /**
     * 成功
     */
    SUCCESS,
    /**
     * 未知原因失败
     */
    UNKNOWN_FAIL,
    /**
     * 射击冷却时间还没到
     */
    @Deprecated
    COOL_DOWN,
    /**
     * 无弹药 (或没有备弹)
     * @deprecated 完全在{@code LocalShooterShoot._onShooterFireFailed()}内部处理
     */
    @Deprecated
    NO_AMMO,
    /**
     * 没有执行切枪逻辑
     */
    NOT_DRAW,
    /**
     * 当前物品不是枪
     */
    NOT_GUN,
    /**
     * 枪械 ID 不存在
     */
    ID_NOT_EXIST,
    /**
     * 需要手动上膛
     * @deprecated 完全在{@code LocalShooterShoot._onShooterFireFailed()}内部处理
     */
    @Deprecated
    NEED_BOLT,
    /**
     * 正处于换弹状态
     */
    @ApiStatus.Internal
    IS_RELOADING,
    /**
     * 正处于切枪状态
     */
    IS_DRAWING,
    /**
     * 正处于拉拴状态
     */
    @ApiStatus.Internal
    IS_BOLTING,
    /**
     * 正处于近战状态
     */
    @ApiStatus.Internal
    IS_MELEE,
    /**
     * 正处于疾跑状态
     * @deprecated 完全在{@link LivingShooterShoot#_shouldForceDisableShoot()}内部处理
     */
    @Deprecated
    IS_SPRINTING,
    /**
     * 网络波动导致射击失败
     * @deprecated 完全在{@link LivingShooterShoot#isInServerShootCooldown(long, long)}内部处理
     */
    @Deprecated
    NETWORK_FAIL,
    /**
     * 事件取消
     * @deprecated Go to {@link IGunAttackRuntime}
     */
    @Deprecated
    EVENT_CANCELED,
    /**
     * 武器过热
     * @see _DefaultGunAttack#shooterFire 生成该结果
     * @deprecated 完全在{@code LocalShooterShoot._onShooterFireFailed()}内部处理
     */
    @Deprecated
    OVERHEATED
}
