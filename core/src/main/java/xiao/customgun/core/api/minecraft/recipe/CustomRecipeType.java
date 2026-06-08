/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.minecraft.recipe;

import net.minecraft.resources.ResourceLocation;
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
    MOD_TABLE_RECIPE(CustomRecipeTypeTag.MOD_TABLE_RECIPE, CustomRecipeTypeTag.MOD_TABLE_RECIPE_OLD1),;

    public final String typeName;
    public final String typeNameOld;
    public final String registryName;
    public final String registryNameOld;
    public final ResourceLocation registryLocation;
    CustomRecipeType(String name, String nameOld) {
        this.typeName = name;
        this.typeNameOld = nameOld;
        this.registryName = String.format("%s:%s", CustomGun.MOD_ID, this.typeName);
        this.registryNameOld = String.format("%s:%s", CustomGun.MOD_ID_OLD1, this.typeNameOld);
        this.registryLocation = CustomGun.getMcRegistry().createResourceLocation(this.registryName);
    }

    @Override public String getTagName() {
        return this.typeName;
    }
    public String getTagNameOld() {
        return this.typeNameOld;
    }
    @Override public String getRegistryName() {
        return this.registryName;
    }
    public String getRegistryNameOld() {
        return this.registryNameOld;
    }
    @Override public ResourceLocation getRegistryLocation() {
        return this.registryLocation;
    }

    private static final Map<String, CustomRecipeType> RECIPE_TYPES = new HashMap<>();

    static {
        for (CustomRecipeType type : values()) {
            RECIPE_TYPES.put(type.typeName, type);
            RECIPE_TYPES.put(type.typeNameOld, type);
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