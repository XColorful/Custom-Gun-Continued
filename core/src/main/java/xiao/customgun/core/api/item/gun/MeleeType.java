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

public enum MeleeType implements ResourceTag.CategoryTag {
    /**
     * TODO 捅/刺刀?
     */
    PUSH(MeleeTypeTag.PUSH),
    /**
     * 枪托
     */
    STOCK(MeleeTypeTag.STOCK);

    public final String typeName;
    MeleeType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, MeleeType> MELEE_TYPES = new HashMap<>();

    static {
        for (MeleeType type : values()) {
            MELEE_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable MeleeType fromString(String name) {
        return name != null ? MELEE_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}