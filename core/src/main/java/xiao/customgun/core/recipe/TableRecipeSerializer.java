/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.recipe;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
import java.util.stream.Stream;

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

    private static final MapCodec<TableRecipe> TABLE_RECIPE_MAP_CODEC = new MapCodec<>() {
        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.empty();
        }
        @Override
        public <T> DataResult<TableRecipe> decode(DynamicOps<T> ops, MapLike<T> input) {
            try {
                T map = ops.createMap(input.entries());
                Dynamic<T> dynamic = new Dynamic<>(ops, map);
                JsonObject jsonObject = dynamic.convert(JsonOps.INSTANCE).getValue().getAsJsonObject();
                return DataResult.success(_fromJson(
                        _dummyLocation,
                        jsonObject));
            } catch (Exception e) {
                return DataResult.error(() -> "Failed to decode TableRecipe: " + e.getMessage());
            }
        }
        @Override
        public <T> RecordBuilder<T> encode(TableRecipe input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return prefix.withErrorsFrom(DataResult.error(() -> "Encoding TableRecipe to JSON is not supported."));
        }
    };
    private static final StreamCodec<RegistryFriendlyByteBuf, TableRecipe> TABLE_RECIPE_STREAM_CODEC = StreamCodec.of(
            (buf, recipe) -> _INSTANCE.toNetwork(buf, recipe),
            buf -> _INSTANCE.fromNetwork(buf)
    );
    @Override
    public @NotNull MapCodec<TableRecipe> codec() {
        return TABLE_RECIPE_MAP_CODEC;
    }
    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, TableRecipe> streamCodec() {
        return TABLE_RECIPE_STREAM_CODEC;
    }
}
