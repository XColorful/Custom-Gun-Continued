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

public enum FireModeType implements ResourceTag.CategoryTag {
    /**
     * 全自动
     */
    AUTO(FireModeTypeTag.AUTO),
    /**
     * 半自动
     */
    SEMI(FireModeTypeTag.SEMI),
    /**
     * 多连发
     */
    BURST(FireModeTypeTag.BURST),
    /**
     * 默认情况
     */
    DEFAULT(FireModeTypeTag.DEFAULT);

    public final String typeName;
    FireModeType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, FireModeType> FIRE_MODES = new HashMap<>();

    static {
        for (FireModeType type : values()) {
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