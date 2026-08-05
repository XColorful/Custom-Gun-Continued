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

public enum AmmoCountType implements ResourceTag.CategoryTag {
    /**
     * 显示实际值
     */
    NORMAL(AmmoCountTypeTag.NORMAL),
    /**
     * 显示百分比
     */
    PERCENT(AmmoCountTypeTag.PERCENT);

    public final String typeName;
    AmmoCountType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, AmmoCountType> AMMO_COUNT_TYPES = new HashMap<>();

    static {
        for (AmmoCountType type : values()) {
            AMMO_COUNT_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable AmmoCountType fromString(String name) {
        return name != null ? AMMO_COUNT_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}