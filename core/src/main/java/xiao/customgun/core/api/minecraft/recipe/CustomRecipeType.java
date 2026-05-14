/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.minecraft.recipe;

import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.recipe.RecipeData;

import java.util.HashMap;
import java.util.Map;

public enum CustomRecipeType implements ResourceTag.RegistryTag {
    /**
     * {@link RecipeData} 解析格式
     */
    TACZ_TABLE_RECIPE(CustomRecipeTypeTag.TACZ_TABLE_RECIPE);

    public final String typeName;
    public final String registryName;
    CustomRecipeType(String name) {
        this.typeName = name;
        this.registryName = String.format("%s:%s", CustomGun.MOD_ID, this.typeName);
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }

    private static final Map<String, CustomRecipeType> RECIPE_TYPES = new HashMap<>();

    static {
        for (CustomRecipeType type : values()) {
            RECIPE_TYPES.put(type.typeName, type);
        }
    }

    public static @Nullable CustomRecipeType fromString(String name) {
        return name != null ? RECIPE_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}