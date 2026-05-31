/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.init.registry;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.init.registry.IRegistrar;
import xiao.customgun.core.api.init.registry.IRegistryObject;
import xiao.customgun.core.api.minecraft.recipe.CustomRecipeType;
import xiao.customgun.core.recipe.TableRecipe;
import xiao.customgun.core.recipe.TableRecipeSerializer;

public class ModRecipe {
    public static final IRegistrar<RecipeSerializer<?>> RECIPE_SERIALIZERS = CustomGun.getRegistrarFactory().createRecipeSerializers(CustomGun.MOD_ID);
    public static final IRegistrar<RecipeSerializer<?>> RECIPE_SERIALIZERS_OLD1 = CustomGun.getRegistrarFactory().createRecipeSerializers(CustomGun.MOD_ID_OLD1);
    public static final IRegistrar<RecipeType<?>> RECIPE_TYPES = CustomGun.getRegistrarFactory().createRecipes(CustomGun.MOD_ID);
    public static final IRegistrar<RecipeType<?>> RECIPE_TYPES_OLD1 = CustomGun.getRegistrarFactory().createRecipes(CustomGun.MOD_ID_OLD1);

    // 类加载顺序会保证在 ↑调用 前执行
    public static final IRegistryObject<RecipeSerializer<? extends TableRecipe>> TABLE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(CustomRecipeType.MOD_TABLE_RECIPE.getTagName(),
            () -> TableRecipeSerializer.REGISTRY_INSTANCE);
    public static final IRegistryObject<RecipeSerializer<? extends TableRecipe>> TABLE_RECIPE_SERIALIZER_OLD1 = RECIPE_SERIALIZERS_OLD1.register(CustomRecipeType.MOD_TABLE_RECIPE.getTagName(),
            () -> TableRecipeSerializer.REGISTRY_INSTANCE_OLD1);
    public static final IRegistryObject<RecipeType<TableRecipe>> TABLE_RECIPE_CRAFTING = RECIPE_TYPES.register(CustomRecipeType.MOD_TABLE_RECIPE.getTagName(), () ->
            new RecipeType<>() {
                @Override
                public String toString() {
                    return CustomRecipeType.MOD_TABLE_RECIPE.getRegistryName();
                }
            });
    public static final IRegistryObject<RecipeType<TableRecipe>> TABLE_RECIPE_CRAFTING_OLD1 = RECIPE_TYPES_OLD1.register(CustomRecipeType.MOD_TABLE_RECIPE.getTagNameOld(), () ->
            new RecipeType<>() {
                @Override
                public String toString() {
                    return CustomRecipeType.MOD_TABLE_RECIPE.getRegistryNameOld();
                }
            });
}
