/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.init.registry.ModRecipe;
import xiao.customgun.core.resource.data.recipe.RecipeData;
import xiao.customgun.core.resource.data.recipe.recipe._TableResultData;

import java.util.List;

public class TableRecipe implements Recipe<Inventory> {
    public static final TableRecipe EMPTY = new TableRecipe(CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID + ":null"),
            TableResult.fromPojo(new _TableResultData()),
            List.of());

    private ResourceLocation recipeLocation;
    private final TableResult tableResult;
    private final List<TableIngredient> recipeIngredients;

    public TableRecipe(ResourceLocation recipeLocation,
                       TableResult tableResult, List<TableIngredient> recipeIngredients) {
        this.recipeLocation = recipeLocation;
        this.tableResult = tableResult;
        this.recipeIngredients = recipeIngredients;
    }
    public static TableRecipe fromPojo(ResourceLocation recipeLocation,
                                       RecipeData pojo) {
        return new TableRecipe(recipeLocation, TableResult.fromPojo(pojo.getTableResult()), TableIngredient.fromPojo(pojo.getTableIngredients()));
    }
    /**
     * @since 1.20.2起 {@link TableRecipeSerializer} 就拿不到 recipeLocation 了
     */
    @ApiStatus.AvailableSince("1.20.2")
    public void setRecipeLocation(ResourceLocation recipeLocation) {
        this.recipeLocation = recipeLocation;
    }

    public void init() {
        this.tableResult.init();
    }

    public ResourceLocation getRecipeLocation() {
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
    public boolean matches(@NotNull Inventory inventory, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull Inventory inventory, @NotNull RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public @NotNull ItemStack getResultItem(@NotNull RegistryAccess registryAccess) {
        return this.tableResult.getResultItem().copy();
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return this.recipeLocation;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return ModRecipe.TABLE_RECIPE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return ModRecipe.TABLE_RECIPE_CRAFTING.get();
    }

    public ItemStack getResultItem() {
        return this.tableResult.getResultItem();
    }
    public ResourceLocation getTabLocation() {
        return this.tableResult.getTabLocation();
    }
}
