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
    public static final IRegistrar<RecipeType<?>> RECIPE_TYPES = CustomGun.getRegistrarFactory().createRecipes(CustomGun.MOD_ID);

    // 类加载顺序会保证在 ↑调用 前执行
    public static final IRegistryObject<RecipeSerializer<? extends TableRecipe>> TACZ_TABLE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(CustomRecipeType.TACZ_TABLE_RECIPE.getTagName(),
            TableRecipeSerializer::new);
    public static final IRegistryObject<RecipeType<TableRecipe>> TACZ_TABLE_RECIPE_CRAFTING = RECIPE_TYPES.register(CustomRecipeType.TACZ_TABLE_RECIPE.getTagName(), () ->
            new RecipeType<>() {
                @Override
                public String toString() {
                    return CustomRecipeType.TACZ_TABLE_RECIPE.getRegistryName();
                }
            });
}
