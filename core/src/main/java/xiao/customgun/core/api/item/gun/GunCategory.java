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

public enum GunCategory implements ResourceTag.CategoryTag {
    /**
     * 霰弹枪
     */
    SHOTGUN(GunCategoryTag.SHOTGUN),
    /**
     * 手枪
     */
    PISTOL(GunCategoryTag.PISTOL),
    /**
     * 步枪
     */
    RIFLE(GunCategoryTag.RIFLE),
    /**
     * 狙击枪
     */
    SNIPER(GunCategoryTag.SNIPER),
    /**
     * 机枪
     */
    MG(GunCategoryTag.MG),
    /**
     * 冲锋枪
     */
    SMG(GunCategoryTag.SMG),
    /**
     * 火箭筒
     */
    RPG(GunCategoryTag.RPG),
    /**
     * 自定义
     */
    CUSTOM(GunCategoryTag.CUSTOM);

    public final String categoryName;
    GunCategory(String name) {
        this.categoryName = name;
    }

    @Override public String getTagName() {
        return this.categoryName;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }

    private static final Map<String, GunCategory> GUN_CATEGORIES = new HashMap<>();

    static {
        for (GunCategory type : values()) {
            GUN_CATEGORIES.put(type.categoryName, type);
        }
    }

    public static @Nullable GunCategory fromString(String name) {
        return name != null ? GUN_CATEGORIES.get(name) : null;
    }
    public String toString() {
        return this.categoryName;
    }
}