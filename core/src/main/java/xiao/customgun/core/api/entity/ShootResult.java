/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.gun.attack.IGunAttackRuntime;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.gun.attack._DefaultGunAttack;

import java.util.function.Supplier;

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
    COOL_DOWN,
    /**
     * 无弹药 (或没有备弹)
     */
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
     */
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
     */
    IS_SPRINTING,
    /**
     * 网络波动导致射击失败
     */
    @ApiStatus.Internal
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
     * @deprecated 完全在{@code LocalShooterShoot.shoot()} (3.) 内部处理
     */
    @Deprecated
    OVERHEATED,
    /**
     * 状态不对
     */
    PRE_CHECK_ERROR;
}
