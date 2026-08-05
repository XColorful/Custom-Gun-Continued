/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.recipe;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.init.registry.ModRecipe;
import dev.xcolorful.customgun.core.resource.data.recipe.RecipeData;
import dev.xcolorful.customgun.core.resource.data.recipe.recipe._TableResultData;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TableRecipe implements Recipe<RecipeInput> {
    public static final TableRecipe EMPTY = new TableRecipe(CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID + ":null"),
            TableResult.fromPojo(new _TableResultData()),
            List.of());

    private Identifier recipeLocation;
    private final TableResult tableResult;
    private final List<TableIngredient> recipeIngredients;

    public TableRecipe(Identifier recipeLocation,
                       TableResult tableResult, List<TableIngredient> recipeIngredients) {
        this.recipeLocation = recipeLocation;
        this.tableResult = tableResult;
        this.recipeIngredients = recipeIngredients;
    }
    public static TableRecipe fromPojo(Identifier recipeLocation,
                                       RecipeData pojo) {
        return new TableRecipe(recipeLocation, TableResult.fromPojo(pojo.getTableResult()), TableIngredient.fromPojo(pojo.getTableIngredients()));
    }
    /**
     * @since 1.20.2起 {@link TableRecipeSerializer} 就拿不到 recipeLocation 了
     */
    @ApiStatus.AvailableSince("1.20.2")
    public void setRecipeLocation(Identifier recipeLocation) {
        this.recipeLocation = recipeLocation;
    }

    public void prepare() {
        this.tableResult.prepare();
    }

    public Identifier getRecipeLocation() {
        return this.recipeLocation;
    }
    public TableResult getTableResult() {
        return this.tableResult;
    }
    public List<TableIngredient> getRecipeIngredients() {
        return this.recipeIngredients;
    }

    @Override
    @Deprecated
    public boolean matches(@NotNull RecipeInput input, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput input, @NotNull HolderLookup.Provider provider) {
        return ItemStack.EMPTY;
    }

    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider provider) {
        return this.tableResult.getResultItem().copy();
    }

    public @NotNull Identifier getId() {
        return this.recipeLocation;
    }

    @Override
    public @NotNull RecipeSerializer<? extends TableRecipe> getSerializer() {
        return ModRecipe.TABLE_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<? extends TableRecipe> getType() {
        return ModRecipe.TABLE_RECIPE_CRAFTING.get();
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    public ItemStack getResultItem() {
        return this.tableResult.getResultItem();
    }
    public Identifier getTabLocation() {
        return this.tableResult.getTabGroupLocation();
    }
}
