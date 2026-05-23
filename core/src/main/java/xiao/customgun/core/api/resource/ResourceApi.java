/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.network.SyncDataCache;
import xiao.customgun.core.recipe.TableRecipe;
import xiao.customgun.core.resource.AllDataManager;
import xiao.customgun.core.resource.DataInstanceManager;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.BlockData;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.index.AmmoIndex;
import xiao.customgun.core.resource.data.index.AttachmentIndex;
import xiao.customgun.core.resource.data.index.BlockIndex;
import xiao.customgun.core.resource.data.index.GunIndex;
import xiao.customgun.core.resource.data.recipefilter.RecipeFilterData;
import xiao.customgun.core.resource.instance.data.AmmoIndexInstance;
import xiao.customgun.core.resource.instance.data.AttachmentIndexInstance;
import xiao.customgun.core.resource.instance.data.BlockIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

import java.util.*;

public class ResourceApi {

    // --------data--------

    public static @Nullable GunData getGunData(Identifier dataLocation) {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunDataManager != null) return dataManager.gunDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.gunData.get(dataLocation);
    }
    public static @Nullable AttachmentData getAttachmentData(Identifier dataLocation) {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentDataManager != null) return dataManager.attachmentDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.attachmentData.get(dataLocation);
    }
    public static @Nullable BlockData getBlockData(Identifier dataLocation) {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockDataManager != null) return dataManager.blockDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.blockData.get(dataLocation);
    }

    // --------index--------

    public static @Nullable GunIndex getGunIndex(Identifier gunLocation) {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunIndexManager != null) return dataManager.gunIndexManager.getPojo(gunLocation);
        else return SyncDataCache.INSTANCE.gunIndex.get(gunLocation);
    }
    public static Set<Map.Entry<Identifier, GunIndex>> getAllGunIndex() {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunIndexManager != null) return dataManager.gunIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.gunIndex.entrySet();
    }
    public static @Nullable AttachmentIndex getAttachmentIndex(Identifier attachmentLocation) {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentIndexManager != null) return dataManager.attachmentIndexManager.getPojo(attachmentLocation);
        else return SyncDataCache.INSTANCE.attachmentIndex.get(attachmentLocation);
    }
    public static Set<Map.Entry<Identifier, AttachmentIndex>> getAllAttachmentIndex() {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentIndexManager != null) return dataManager.attachmentIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.attachmentIndex.entrySet();
    }
    public static @Nullable AmmoIndex getAmmoIndex(Identifier ammoLocation) {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.ammoIndexManager != null) return dataManager.ammoIndexManager.getPojo(ammoLocation);
        else return SyncDataCache.INSTANCE.ammoIndex.get(ammoLocation);
    }
    public static Set<Map.Entry<Identifier, AmmoIndex>> getAllAmmoIndex() {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.ammoIndexManager != null) return dataManager.ammoIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.ammoIndex.entrySet();
    }
    public static @Nullable BlockIndex getBlockIndex(Identifier blockLocation) {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockIndexManager != null) return dataManager.blockIndexManager.getPojo(blockLocation);
        else return SyncDataCache.INSTANCE.blockIndex.get(blockLocation);
    }
    public static Set<Map.Entry<Identifier, BlockIndex>> getAllBlockIndex() {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockIndexManager != null) return dataManager.blockIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.blockIndex.entrySet();
    }

    // --------recipe--------

    public static @Nullable RecipeManager getRecipeManager() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            var dataManager = AllDataManager.getCurrent();
            return dataManager != null ? dataManager.recipeManager : CustomGun.getMinecraftServer().getRecipeManager();
        } else { // 逻辑客户端
            return ClientResourceApi.getRecipeManager();
        }
    }

    public static @Nullable TableRecipe getTableRecipe(Identifier recipeLocation) {
        @Nullable RecipeManager recipeManager = ResourceApi.getRecipeManager();
        if (recipeManager == null) return null;
        return recipeManager.byKey(CustomGun.getMcRegistry().createResourceKey(Registries.RECIPE, recipeLocation))
                .map(RecipeHolder::value)
                .filter(TableRecipe.class::isInstance)
                .map(TableRecipe.class::cast)
                .orElse(null);
    }
    public static Map<Identifier, TableRecipe> getAllTableRecipe() {
        @Nullable RecipeManager recipeManager = ResourceApi.getRecipeManager();
        if (recipeManager == null) return new HashMap<>();
        Map<Identifier, TableRecipe> tableRecipes = new HashMap<>();
        recipeManager.getRecipes().stream()
                .filter(holder -> holder.value() instanceof TableRecipe)
                .forEach(holder -> {
                    TableRecipe tableRecipe = (TableRecipe) holder.value();
                    tableRecipe.setRecipeLocation(holder.id().identifier());
                    tableRecipes.put(tableRecipe.getRecipeLocation(), tableRecipe);
                });
        return tableRecipes;
    }

    // --------recipe filter--------

    public static @Nullable RecipeFilterData getRecipeFilterData(Identifier filterLocation) {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null && dataManager.recipeFilterDataManager != null) return dataManager.recipeFilterDataManager.getPojo(filterLocation);
        else return SyncDataCache.INSTANCE.recipeFilterData.get(filterLocation);
    }

    // --------data instance--------

    public static @Nullable GunIndexInstance getGunIndexInstance(Identifier gunLocation) {
        return DataInstanceManager.GUN_INDEX.get(gunLocation);
    }
    public static @Nullable AttachmentIndexInstance getAttachmentIndexInstance(Identifier attachmentLocation) {
        return DataInstanceManager.ATTACHMENT_INDEX.get(attachmentLocation);
    }
    public static @Nullable AmmoIndexInstance getAmmoIndexInstance(Identifier ammoLocation) {
        return DataInstanceManager.AMMO_INDEX.get(ammoLocation);
    }
    public static @Nullable BlockIndexInstance getBlockIndexInstance(Identifier blockLocation) {
        return DataInstanceManager.BLOCK_INDEX.get(blockLocation);
    }
}
