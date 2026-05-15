/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.recipe;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.resource.data.recipe.RecipeData;
import xiao.customgun.core.util.JsonUtils;
import xiao.customgun.core.util.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TableRecipeSerializer implements RecipeSerializer<TableRecipe> {

    @Override
    public @NotNull TableRecipe fromJson(@NotNull ResourceLocation recipeLocation, @NotNull JsonObject jsonObject) {
        try (JsonReader reader = JsonUtils.getAsReader(jsonObject)) {
            RecipeData pojo = RecipeData.fromJson(reader);
            if (pojo != null) {
                return TableRecipe.fromPojo(recipeLocation, pojo);
            }
        } catch (IOException e) {
            CustomGun.LOGGER.error("TableRecipeSerializer: Failed to parse RecipeData (RecipeLocation {}) from jsonObject: {} {}", recipeLocation, e, jsonObject);
        }
        return TableRecipe.EMPTY;
    }

    @Override
    public @Nullable TableRecipe fromNetwork(@NotNull ResourceLocation recipeLocation, @NotNull FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<TableIngredient> recipeIngredients = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Ingredient ingredient = NetworkUtils.readIngredient(buffer);
            int ingredientCount = buffer.readInt();
            recipeIngredients.add(new TableIngredient(ingredient, ingredientCount));
        }
        ItemStack resultItem = NetworkUtils.readItem(buffer);
        var tabLocation = NetworkUtils.readResourceLocation(buffer);
        TableResult tableResult = new TableResult(resultItem, tabLocation);
        return new TableRecipe(recipeLocation, tableResult, recipeIngredients);
    }

    @Override
    public void toNetwork(@NotNull FriendlyByteBuf buffer, @NotNull TableRecipe tableRecipe) {
        buffer.writeInt(tableRecipe.getRecipeIngredients().size());
        for (TableIngredient tableIngredient : tableRecipe.getRecipeIngredients()) {
            NetworkUtils.writeIngredient(buffer, tableIngredient.ingredient());
            buffer.writeInt(tableIngredient.count());
        }
        TableResult tableResult = tableRecipe.getTableResult();
        NetworkUtils.writeItem(buffer, tableResult.getResultItem());
        NetworkUtils.writeResourceLocation(buffer, tableResult.getTabLocation());
    }
}
