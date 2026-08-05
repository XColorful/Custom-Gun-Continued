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

public enum FireModeType implements ResourceTag.CategoryTag, ResourceTag.IndexTag {
    /**
     * 全自动
     */
    AUTO(0, FireModeTypeTag.AUTO),
    /**
     * 半自动
     */
    SEMI(1, FireModeTypeTag.SEMI),
    /**
     * 多连发
     */
    BURST(2, FireModeTypeTag.BURST),
    /**
     * 默认情况
     */
    DEFAULT(3, FireModeTypeTag.DEFAULT);

    public final int index;
    public final String typeName;
    FireModeType(int index, String name) {
        this.index = index;
        this.typeName = name;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }
    @Override
    public int getIndex() {
        return this.index;
    }

    private static final Map<String, FireModeType> FIRE_MODES = new HashMap<>();

    static {
        for (FireModeType type : values()) {
            FIRE_MODES.put(String.valueOf(type.index), type);
            FIRE_MODES.put(type.typeName, type);
        }
    }

    public static @Nullable FireModeType fromString(String name) {
        return name != null ? FIRE_MODES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}