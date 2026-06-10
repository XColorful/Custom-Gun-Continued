/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.network.SyncDataCache;
import xiao.customgun.core.init.registry.ModRecipe;
import xiao.customgun.core.recipe.TableRecipe;
import xiao.customgun.core.resource._AllDataManager;
import xiao.customgun.core.resource._DataInstanceManager;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.BlockData;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.index.AmmoIndex;
import xiao.customgun.core.resource.data.index.AttachmentIndex;
import xiao.customgun.core.resource.data.index.BlockIndex;
import xiao.customgun.core.resource.data.index.GunIndex;
import xiao.customgun.core.resource.data.recipefilter.RecipeFilterData;
import xiao.customgun.core.resource.data.script.DataScript;
import xiao.customgun.core.resource.instance.data.AmmoIndexInstance;
import xiao.customgun.core.resource.instance.data.AttachmentIndexInstance;
import xiao.customgun.core.resource.instance.data.BlockIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ResourceApi {

    // --------data--------

    public static @Nullable GunData getGunData(ResourceLocation dataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunDataManager != null) return dataManager.gunDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.gunData.get(dataLocation);
    }
    public static @Nullable AttachmentData getAttachmentData(ResourceLocation dataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentDataManager != null) return dataManager.attachmentDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.attachmentData.get(dataLocation);
    }
    public static @Nullable BlockData getBlockData(ResourceLocation dataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockDataManager != null) return dataManager.blockDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.blockData.get(dataLocation);
    }

    // --------index--------

    public static @Nullable GunIndex getGunIndex(ResourceLocation gunLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunIndexManager != null) return dataManager.gunIndexManager.getPojo(gunLocation);
        else return SyncDataCache.INSTANCE.gunIndex.get(gunLocation);
    }
    public static Set<Map.Entry<ResourceLocation, GunIndex>> getAllGunIndex() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunIndexManager != null) return dataManager.gunIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.gunIndex.entrySet();
    }
    public static @Nullable AttachmentIndex getAttachmentIndex(ResourceLocation attachmentLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentIndexManager != null) return dataManager.attachmentIndexManager.getPojo(attachmentLocation);
        else return SyncDataCache.INSTANCE.attachmentIndex.get(attachmentLocation);
    }
    public static Set<Map.Entry<ResourceLocation, AttachmentIndex>> getAllAttachmentIndex() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentIndexManager != null) return dataManager.attachmentIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.attachmentIndex.entrySet();
    }
    public static @Nullable AmmoIndex getAmmoIndex(ResourceLocation ammoLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.ammoIndexManager != null) return dataManager.ammoIndexManager.getPojo(ammoLocation);
        else return SyncDataCache.INSTANCE.ammoIndex.get(ammoLocation);
    }
    public static Set<Map.Entry<ResourceLocation, AmmoIndex>> getAllAmmoIndex() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.ammoIndexManager != null) return dataManager.ammoIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.ammoIndex.entrySet();
    }
    public static @Nullable BlockIndex getBlockIndex(ResourceLocation blockLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockIndexManager != null) return dataManager.blockIndexManager.getPojo(blockLocation);
        else return SyncDataCache.INSTANCE.blockIndex.get(blockLocation);
    }
    public static Set<Map.Entry<ResourceLocation, BlockIndex>> getAllBlockIndex() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockIndexManager != null) return dataManager.blockIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.blockIndex.entrySet();
    }

    // --------recipe--------

    public static @Nullable RecipeManager getRecipeManager() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            var dataManager = _AllDataManager.getCurrent();
            return dataManager != null ? dataManager.recipeManager : CustomGun.getMinecraftServer().getRecipeManager();
        } else { // 逻辑客户端
            return ClientResourceApi.getRecipeManager();
        }
    }

    public static @Nullable TableRecipe getTableRecipe(ResourceLocation recipeLocation) {
        @Nullable RecipeManager recipeManager = ResourceApi.getRecipeManager();
        if (recipeManager == null) return null;
        return (TableRecipe) recipeManager.byKey(recipeLocation).filter(recipe -> recipe instanceof TableRecipe).orElse(null);
    }
    public static Map<ResourceLocation, TableRecipe> getAllTableRecipe() {
        @Nullable RecipeManager recipeManager = ResourceApi.getRecipeManager();
        if (recipeManager == null) return new HashMap<>();
        Map<ResourceLocation, TableRecipe> tableRecipes = new HashMap<>();
        for (TableRecipe recipe : recipeManager.getAllRecipesFor(ModRecipe.TABLE_RECIPE_CRAFTING.get())) {
            tableRecipes.put(recipe.getRecipeLocation(), recipe);
        }
        return tableRecipes;
    }

    // --------recipe filter--------

    public static @Nullable RecipeFilterData getRecipeFilterData(ResourceLocation filterLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.recipeFilterDataManager != null) return dataManager.recipeFilterDataManager.getPojo(filterLocation);
        else return SyncDataCache.INSTANCE.recipeFilterData.get(filterLocation);
    }

    // --------script--------

    public static @Nullable DataScript getDataScript(ResourceLocation scriptLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.scriptManager != null) return dataManager.scriptManager.getFile(scriptLocation);
        else return null;
    }
    public static Set<Map.Entry<ResourceLocation, DataScript>> getAllDataScript() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.scriptManager != null) return dataManager.scriptManager.getAllFiles().entrySet();
        else return new HashSet<>();
    }

    // --------data instance--------

    public static @Nullable GunIndexInstance getGunIndexInstance(ResourceLocation gunLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.GUN_INDEX.get(gunLocation);
        else return SyncDataCache.INSTANCE.GUN_INDEX.get(gunLocation);
    }
    public static Set<Map.Entry<ResourceLocation, GunIndexInstance>> getAllGunIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.GUN_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.GUN_INDEX.entrySet();
    }
    public static @Nullable AttachmentIndexInstance getAttachmentIndexInstance(ResourceLocation attachmentLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_INDEX.get(attachmentLocation);
        else return SyncDataCache.INSTANCE.ATTACHMENT_INDEX.get(attachmentLocation);
    }
    public static Set<Map.Entry<ResourceLocation, AttachmentIndexInstance>> getAllAttachmentIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.ATTACHMENT_INDEX.entrySet();
    }
    public static @Nullable AmmoIndexInstance getAmmoIndexInstance(ResourceLocation ammoLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.AMMO_INDEX.get(ammoLocation);
        else return SyncDataCache.INSTANCE.AMMO_INDEX.get(ammoLocation);
    }
    public static Set<Map.Entry<ResourceLocation, AmmoIndexInstance>> getAllAmmoIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.AMMO_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.AMMO_INDEX.entrySet();
    }
    public static @Nullable BlockIndexInstance getBlockIndexInstance(ResourceLocation blockLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.BLOCK_INDEX.get(blockLocation);
        else return SyncDataCache.INSTANCE.BLOCK_INDEX.get(blockLocation);
    }
    public static Set<Map.Entry<ResourceLocation, BlockIndexInstance>> getAllBlockIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.BLOCK_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.BLOCK_INDEX.entrySet();
    }
}
