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

/**
 * 供弹类型枚举
 */
public enum AmmoFeedType implements ResourceTag.CategoryTag {
    /**
     * 弹匣供弹
     */
    MAGAZINE(AmmoFeedTypeTag.MAGAZINE),
    /**
     * 手动供弹
     */
    MANUAL(AmmoFeedTypeTag.MANUAL),
    /**
     * 燃料供弹(消耗单个物品补满弹药)
     */
    FUEL(AmmoFeedTypeTag.FUEL),
    /**
     * 背包直读(直接消耗背包内弹药)
     */
    INVENTORY(AmmoFeedTypeTag.INVENTORY);

    public final String typeName;

    AmmoFeedType(String name) {
        this.typeName = name;
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getCategoryName() {
        return this.typeName;
    }

    private static final Map<String, AmmoFeedType> AMMO_FEED_TYPES = new HashMap<>();

    static {
        for (AmmoFeedType type : values()) {
            AMMO_FEED_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable AmmoFeedType fromString(String name) {
        return name != null ? AMMO_FEED_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}