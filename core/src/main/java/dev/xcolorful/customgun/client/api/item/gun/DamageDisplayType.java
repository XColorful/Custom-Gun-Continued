/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.item.gun;

import dev.xcolorful.customgun.core.api.item.gun.DamageDisplayTypeTag;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum DamageDisplayType implements ResourceTag.CategoryTag {
    /**
     * 总伤害
     */
    TOTAL(DamageDisplayTypeTag.TOTAL),
    /**
     * 单发x分裂数
     */
    PER_PROJECTILE(DamageDisplayTypeTag.PER_PROJECTILE);

    public final String typeName;
    DamageDisplayType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, DamageDisplayType> DISPLAY_TYPES = new HashMap<>();

    static {
        for (DamageDisplayType type : values()) {
            DISPLAY_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable DamageDisplayType fromString(String name) {
        return name != null ? DISPLAY_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}