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
    AUTO(ChargeTypeTag.AUTO),
    /**
     * 按住扳机蓄力，直到松开扳机才发射（如果满足阈值）
     */
    HOLD(ChargeTypeTag.HOLD),
    /**
     * 按下扳机后自动进行蓄力，蓄满后自动发射，无法取消
     */
    DELAY(ChargeTypeTag.DELAY);

    public final String typeName;
    ChargeType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
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