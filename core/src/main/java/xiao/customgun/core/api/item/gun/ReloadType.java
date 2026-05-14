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

public enum ReloadType implements ResourceTag.CategoryTag {
    /**
     * 空仓换弹
     */
    EMPTY(ReloadTypeTag.EMPTY),
    /**
     * 战术换弹
     */
    TACTICAL(ReloadTypeTag.TACTICAL);

    public final String typeName;
    ReloadType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, ReloadType> RELOAD_TYPES = new HashMap<>();

    static {
        for (ReloadType type : values()) {
            RELOAD_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable ReloadType fromString(String name) {
        return name != null ? RELOAD_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}