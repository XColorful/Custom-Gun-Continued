/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ReloadState;

public interface ISynGunState {

    /**
     * 获取从服务端同步的射击的冷却
     */
    long cgc$getSynShootCooldown();

    /**
     * 获取从服务端同步的近战的冷却（主要是刺刀）
     */
    long cgc$getSynMeleeCooldown();

    /**
     * 获取从服务端同步的切枪的冷却
     */
    long cgc$getSynDrawCooldown();

    /**
     * 获取从服务端同步的手动换弹的冷却
     */
    boolean cgc$getSynIsBolting();

    /**
     * 获取从服务端同步的换弹状态
     */
    ReloadState cgc$getSynReloadState();

    /**
     * 获取从服务端同步的瞄准进度
     */
    float cgc$getSynAimingProgress();

    /**
     * 获取该实体是否正在瞄准
     * 注意，这个方法并不等价于 getSynAimingProgress() > 0
     * 如果玩家正在瞄准，瞄准进度会增加，否则瞄准进度会减少
     */
    boolean cgc$getSynIsAiming();

    /**
     * 获取玩家持枪奔跑的时长。
     * 最大不会大于枪械数据中设置的 sprintTime，最小不会小于 0
     */
    float cgc$getSynSprintTime();
}
