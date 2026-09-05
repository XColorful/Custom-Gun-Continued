/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.gun;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum ChargeType implements ResourceTag.CategoryTag {
    /**
     * 按住扳机蓄力，蓄满后自动发射
     */
    AUTO(ChargeTypeTag.AUTO,
            true, false, true),
    /**
     * 按住扳机蓄力，直到松开扳机才发射（如果满足阈值）
     */
    HOLD(ChargeTypeTag.HOLD,
            true, true, false),
    /**
     * 按下扳机后自动进行蓄力，蓄满后自动发射，无法取消
     */
    DELAY(ChargeTypeTag.DELAY,
            false, false, true);

    public final String typeName;
    public final boolean needHolding;
    public final boolean manualShoot;
    public final boolean needMaxCharge;
    ChargeType(String name, boolean needHolding, boolean manualShoot, boolean needMaxCharge) {
        this.typeName = name;
        this.needHolding = needHolding;
        this.manualShoot = manualShoot;
        this.needMaxCharge = needMaxCharge;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    /**
     * @return 是否需要长按蓄力(充能)
     */
    public boolean needHolding() {
        return this.needHolding;
    }
    /**
     * @return 是否在开始蓄力后就持续增长
     */
    public boolean unstoppableIfStarted() {
        return !this.needHolding;
    }
    public boolean resetChargeAfterShoot() {
        return unstoppableIfStarted();
    }
    /**
     * @return 是否在蓄满后自动开火
     */
    public boolean autoShootIfCharged() {
        return !this.manualShoot;
    }
    /**
     * @return 是否能在部分蓄力状态(达到阈值)下开火
     */
    public boolean partialShoot() {
        return !this.needMaxCharge;
    }

    private static final Map<String, ChargeType> CHARGE_TYPES = new HashMap<>();

    static {
        for (ChargeType type : values()) {
            CHARGE_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable ChargeType fromString(String name) {
        return name != null ? CHARGE_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}