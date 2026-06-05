/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.entity.shooter;

public interface ILocalShooterState {

    /**
     * 客户端是否处于瞄准状态
     */
    boolean cgc$isAim();

    /**
     * 是否爬行
     */
    boolean cgc$isCrawl();

    boolean cgc$isReadyToDraw();

    boolean cgc$isCharging();

    /**
     * 客户端瞄准进度
     * @return 0-1，1 代表开镜进度到 100%
     */
    float cgc$getAimingProgress(float partialTicks);

    /**
     * 客户端射击冷却时间
     */
    long cgc$getShootCooldown();

    float cgc$getChargeProgress();
}