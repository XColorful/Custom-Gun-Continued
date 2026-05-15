/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.recipe;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
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
    // 1.21.1+
    public static final TableRecipeSerializer _INSTANCE = new TableRecipeSerializer();
    public static final ResourceLocation _dummyLocation = CustomGun.getMcRegistry().createResourceLocation(String.format("%s:%s", CustomGun.MOD_ID, "dynamic_recipe"));

    public @NotNull TableRecipe fromJson(@NotNull ResourceLocation recipeLocation, @NotNull JsonObject jsonObject) {
        return _fromJson(recipeLocation, jsonObject);
    }
    // 1.20.2+
    public static @NotNull TableRecipe _fromJson(@NotNull ResourceLocation recipeLocation, @NotNull JsonObject jsonObject) {
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
    public @Nullable TableRecipe fromNetwork(@NotNull FriendlyByteBuf buffer) {
        var recipeLocation = NetworkUtils.readResourceLocation(buffer);
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
        NetworkUtils.writeResourceLocation(buffer, tableRecipe.getId());
        buffer.writeInt(tableRecipe.getRecipeIngredients().size());
        for (TableIngredient tableIngredient : tableRecipe.getRecipeIngredients()) {
            NetworkUtils.writeIngredient(buffer, tableIngredient.ingredient());
            buffer.writeInt(tableIngredient.count());
        }
        TableResult tableResult = tableRecipe.getTableResult();
        NetworkUtils.writeItem(buffer, tableResult.getResultItem());
        NetworkUtils.writeResourceLocation(buffer, tableResult.getTabLocation());
    }

    @Override
    public @NotNull Codec<TableRecipe> codec() {
        return CODEC;
    }
    private static final Codec<TableRecipe> CODEC = Codec.PASSTHROUGH.flatXmap(dynamic -> {
        try {
            CustomGun.LOGGER.debug("TableRecipeSerializer: Loading dynamic recipe from {}", dynamic);
            JsonObject jsonObject = dynamic.convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
            return DataResult.success(staticFromJson(
                    CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID + ":dynamic_recipe"),
                    jsonObject));
        } catch (Exception e) {
            return DataResult.error(() -> "Failed to decode TableRecipe: " + e.getMessage());
        }
    }, recipe -> DataResult.error(() -> "Encoding TableRecipe to JSON is not supported."));
    private static TableRecipe staticFromJson(ResourceLocation id, JsonObject json) {
        try (JsonReader reader = JsonUtils.getAsReader(json)) {
            RecipeData pojo = RecipeData.fromJson(reader);
            if (pojo != null) return TableRecipe.fromPojo(id, pojo);
        } catch (IOException e) {
            CustomGun.LOGGER.error("TableRecipeSerializer: Parse failed for {}", id);
        }
        return TableRecipe.EMPTY;
    }
}
