/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum ChargeType implements ResourceTag.CategoryTag {
    /**
     * 按住扳机蓄力，蓄满后自动发射
     */
    AUTO(ChargeTypeTag.AUTO, true, false),
    /**
     * 按住扳机蓄力，直到松开扳机才发射（如果满足阈值）
     */
    HOLD(ChargeTypeTag.HOLD, true, true),
    /**
     * 按下扳机后自动进行蓄力，蓄满后自动发射，无法取消
     */
    DELAY(ChargeTypeTag.DELAY, false, false);

    public final String typeName;
    public final boolean needHolding;
    public final boolean manualShoot;
    ChargeType(String name, boolean needHolding, boolean manualShoot) {
        this.typeName = name;
        this.needHolding = needHolding;
        this.manualShoot = manualShoot;
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
    /**
     * @return 是否在蓄满后自动开火
     */
    public boolean autoShootIfCharged() {
        return !this.manualShoot;
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