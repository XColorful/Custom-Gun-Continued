/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.init.registry;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.init.registry.IRegistrar;
import dev.xcolorful.customgun.core.api.init.registry.IRegistryObject;
import dev.xcolorful.customgun.core.api.minecraft.recipe.CustomRecipeType;
import dev.xcolorful.customgun.core.recipe.TableRecipe;
import dev.xcolorful.customgun.core.recipe.TableRecipeSerializer;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipe {
    public static final IRegistrar<RecipeSerializer<?>> RECIPE_SERIALIZERS = CustomGun.getRegistrarFactory().createRecipeSerializers(CustomGun.MOD_ID);
    public static final IRegistrar<RecipeSerializer<?>> RECIPE_SERIALIZERS_OLD1 = CustomGun.getRegistrarFactory().createRecipeSerializers(CustomGun.MOD_ID_OLD1);
    public static final IRegistrar<RecipeType<?>> RECIPE_TYPES = CustomGun.getRegistrarFactory().createRecipes(CustomGun.MOD_ID);
    public static final IRegistrar<RecipeType<?>> RECIPE_TYPES_OLD1 = CustomGun.getRegistrarFactory().createRecipes(CustomGun.MOD_ID_OLD1);

    
    public static final IRegistryObject<RecipeSerializer<? extends TableRecipe>> TABLE_RECIPE_SERIALIZER = RECIPE_SERIALIZERS.register(CustomRecipeType.MOD_TABLE_RECIPE.getTagName(),
            TableRecipeSerializer::new);
    public static final IRegistryObject<RecipeSerializer<? extends TableRecipe>> TABLE_RECIPE_SERIALIZER_OLD1 = RECIPE_SERIALIZERS_OLD1.register(CustomRecipeType.MOD_TABLE_RECIPE.getTagNameOld(),
            TableRecipeSerializer::new);
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
