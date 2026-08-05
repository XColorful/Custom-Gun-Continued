/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.item.gun;

import dev.xcolorful.customgun.core.api.item.gun.HudDisplayTypeTag;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum HudDisplayType implements ResourceTag.CategoryTag {
    /**
     * 普通 HUD 显示
     */
    NORMAL(HudDisplayTypeTag.NORMAL),
    /**
     * 空仓 HUD 显示
     */
    EMPTY(HudDisplayTypeTag.EMPTY);

    public final String typeName;
    HudDisplayType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, HudDisplayType> HUD_DISPLAY_TYPES = new HashMap<>();

    static {
        for (HudDisplayType type : values()) {
            HUD_DISPLAY_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable HudDisplayType fromString(String name) {
        return name != null ? HUD_DISPLAY_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}