/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;

public abstract class LivingShooterAspect {
    @ApiStatus.Internal public static final int WINDOW_TIME_MS = 5;
    @ApiStatus.Internal public static final int NETWORK_DELAY_MS = 300;
    @ApiStatus.Internal public static final float CHARGE_PROGRESS_TOLERANCE = 0.001f;
    @ApiStatus.Internal public static final int CHARGE_TICK_TOLERANCE = 4;
    @ApiStatus.Internal public static final int RELOAD_COOLDOWN_MS = 50;
    /**
     * 从指定高度处下落，不会取消趴姿的最小值:
     * <ul>
     *     <li>0.5格 -> 4 ticks</li>
     *     <li>1.0格 -> 5 ticks</li>
     *     <li>1.5格 -> 6 ticks</li>
     *     <li>2.0格 -> 7 ticks</li>
     * </ul>
     * 从指定高度处跳起再下落到地面，不会取消趴姿的最小值:
     * <ul>
     *     <li>0.0格 -> 12 ticks</li>
     *     <li>0.5格 -> 13 ticks</li>
     * </ul>
     */
    @ApiStatus.Internal public static final int NOT_ON_GROUND_DISABLE_PRONE_TICKS = 5;
    /**
     * <ul>
     *     <li>用于防止在趴地时长不够时，起跳，会因为没到{@link #NOT_ON_GROUND_DISABLE_PRONE_TICKS}而保持趴姿，从而导致碰撞箱抽搐(视觉观感不好)</li>
     *     <li>测试下来临界值在17，设成18看着比较舒服的数值</li>
     *     <li>即使设置到18，也可以触发抽搐</li>
     * </ul>
     */
    @ApiStatus.Internal public static final int PRONE_ANIMATION_TICKS = 18;

    protected final LivingEntity livingShooter;
    protected final ShooterProperty shooterProperty;

    public LivingShooterAspect(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        this.livingShooter = livingShooter;
        this.shooterProperty = shooterProperty;
    }
}
