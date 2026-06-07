/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum GunCategory implements ResourceTag.CategoryTag {
    /**
     * 霰弹枪
     */
    SHOTGUN(GunCategoryTag.SHOTGUN,
            Component.translatable("customgun.guncategory.shotgun")),
    /**
     * 手枪
     */
    PISTOL(GunCategoryTag.PISTOL,
            Component.translatable("customgun.guncategory.pistol")),
    /**
     * 步枪
     */
    RIFLE(GunCategoryTag.RIFLE,
            Component.translatable("customgun.guncategory.rifle")),
    /**
     * 狙击枪
     */
    SNIPER(GunCategoryTag.SNIPER,
            Component.translatable("customgun.guncategory.sniper")),
    /**
     * 机枪
     */
    MG(GunCategoryTag.MG,
            Component.translatable("customgun.guncategory.mg")),
    /**
     * 冲锋枪
     */
    SMG(GunCategoryTag.SMG,
            Component.translatable("customgun.guncategory.smg")),
    /**
     * 火箭筒
     */
    RPG(GunCategoryTag.RPG,
            Component.translatable("customgun.guncategory.rpg")),
    /**
     * 自定义
     */
    CUSTOM(GunCategoryTag.CUSTOM,
            Component.translatable("customgun.guncategory.custom"));

    public final String categoryName;
    public final MutableComponent categoryLang;
    GunCategory(String name, MutableComponent lang) {
        this.categoryName = name;
        this.categoryLang = lang;
    }

    @Override public String getTagName() {
        return this.categoryName;
    }
    @Override public String getCategoryName() {
        return this.categoryName;
    }

    public final MutableComponent getCategoryLang() {
        return this.categoryLang;
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