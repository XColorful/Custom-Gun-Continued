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

public enum BoltType implements ResourceTag.CategoryTag, ResourceTag.IndexTag {
    /**
     * 开膛待击 (枪管不留子弹)
     */
    OPEN_BOLT(3, BoltTypeTag.OPEN_BOLT),
    /**
     * 闭膛待击 ("+1"弹匣)
     */
    CLOSED_BOLT(2, BoltTypeTag.CLOSED_BOLT),
    /**
     * 手动上膛 (栓狙)
     */
    MANUAL_ACTION(1, BoltTypeTag.MANUAL_ACTION);

    public final int index;
    public final String typeName;
    BoltType(int index, String name) {
        this.index = index;
        this.typeName = name;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }
    @Override public int getIndex() {
        return this.index;
    }

    private static final Map<String, BoltType> BOLT_TYPES = new HashMap<>();

    static {
        for (BoltType type : values()) {
            BOLT_TYPES.put(String.valueOf(type.index), type);
            BOLT_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable BoltType fromString(String name) {
        return name != null ? BOLT_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}