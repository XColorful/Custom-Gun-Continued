/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.resource.network.SyncDataCache;
import dev.xcolorful.customgun.core.recipe.TableRecipe;
import dev.xcolorful.customgun.core.resource._AllDataManager;
import dev.xcolorful.customgun.core.resource._DataInstanceManager;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.BlockData;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.data.index.BlockIndex;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.data.meta.GunpackMeta;
import dev.xcolorful.customgun.core.resource.data.modtags.AttachmentTagData;
import dev.xcolorful.customgun.core.resource.data.modtags.GunAttachmentData;
import dev.xcolorful.customgun.core.resource.data.recipefilter.RecipeFilterData;
import dev.xcolorful.customgun.core.resource.data.script.DataScript;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.BlockIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.ClassUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public static Set<Map.Entry<ResourceLocation, GunData>> getAllGunData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunDataManager != null) return dataManager.gunDataManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.gunData.entrySet();
    }
    public static @Nullable AttachmentData getAttachmentData(ResourceLocation dataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentDataManager != null) return dataManager.attachmentDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.attachmentData.get(dataLocation);
    }
    public static Set<Map.Entry<ResourceLocation, AttachmentData>> getAllAttachmentData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentDataManager != null) return dataManager.attachmentDataManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.attachmentData.entrySet();
    }
    public static @Nullable BlockData getBlockData(ResourceLocation dataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockDataManager != null) return dataManager.blockDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.blockData.get(dataLocation);
    }
    public static Set<Map.Entry<ResourceLocation, BlockData>> getAllBlockData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockDataManager != null) return dataManager.blockDataManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.blockData.entrySet();
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

    // --------meta--------

    public static @Nullable GunpackMeta getGunpackMeta(ResourceLocation gunpackMetaLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunpackMetaManager != null) return dataManager.gunpackMetaManager.getPojo(gunpackMetaLocation);
        else return null;
    }
    public static Set<Map.Entry<ResourceLocation, GunpackMeta>> getAllGunpackMeta() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunpackMetaManager != null) return dataManager.gunpackMetaManager.getAllPojo().entrySet();
        else return new HashSet<>();
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
        return recipeManager.byKey(CustomGun.getMcRegistry().createResourceKey(Registries.RECIPE, recipeLocation))
                .map(RecipeHolder::value)
                .filter(TableRecipe.class::isInstance)
                .map(TableRecipe.class::cast)
                .orElse(null);
    }
    public static Map<ResourceLocation, TableRecipe> getAllTableRecipe() {
        @Nullable RecipeManager recipeManager = ResourceApi.getRecipeManager();
        if (recipeManager == null) return new HashMap<>();
        Map<ResourceLocation, TableRecipe> tableRecipes = new HashMap<>();
        recipeManager.getRecipes().stream()
                .filter(holder -> holder.value() instanceof TableRecipe)
                .forEach(holder -> {
                    TableRecipe tableRecipe = (TableRecipe) holder.value();
                    tableRecipe.setRecipeLocation(holder.id().location());
                    tableRecipes.put(tableRecipe.getRecipeLocation(), tableRecipe);
                });
        return tableRecipes;
    }

    // --------recipe filter--------

    public static @Nullable RecipeFilterData getRecipeFilterData(ResourceLocation filterLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.recipeFilterDataManager != null) return dataManager.recipeFilterDataManager.getPojo(filterLocation);
        else return SyncDataCache.INSTANCE.recipeFilterData.get(filterLocation);
    }
    public static Set<Map.Entry<ResourceLocation, RecipeFilterData>> getAllRecipeFilterData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.recipeFilterDataManager != null) return dataManager.recipeFilterDataManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.recipeFilterData.entrySet();
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

    // --------modtags--------

    public static @Nullable AttachmentTagData getAttachmentTagData(ResourceLocation attachmentTagDataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentTagManager != null) return dataManager.attachmentTagManager.getPojo(attachmentTagDataLocation);
        else return SyncDataCache.INSTANCE.attachmentTagData.get(attachmentTagDataLocation);
    }
    public static Set<Map.Entry<ResourceLocation, AttachmentTagData>> getAllAttachmentTagData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentTagManager != null) return dataManager.attachmentTagManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.attachmentTagData.entrySet();
    }
    public static @Nullable GunAttachmentData getGunAttachmentData(ResourceLocation gunAttachmentDataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunAttachmentDataManager != null) return dataManager.gunAttachmentDataManager.getPojo(gunAttachmentDataLocation);
        else return SyncDataCache.INSTANCE.gunAttachmentData.get(gunAttachmentDataLocation);
    }
    public static Set<Map.Entry<ResourceLocation, GunAttachmentData>> getAllGunAttachmentData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunAttachmentDataManager != null) return dataManager.gunAttachmentDataManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.gunAttachmentData.entrySet();
    }

    // --------data instance--------

    public static @Nullable GunIndexInstance getGunIndexInstance(ResourceLocation gunLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.GUN_INDEX.get(gunLocation);
        else return SyncDataCache.INSTANCE.GUN_INDEX.get(gunLocation);
    }
    public static Set<Map.Entry<ResourceLocation, @NotNull GunIndexInstance>> getAllGunIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.GUN_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.GUN_INDEX.entrySet();
    }
    public static @Nullable Integer getGunSort(ResourceLocation gunLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.GUN_SORT.getGunSort(gunLocation);
        else return SyncDataCache.INSTANCE.GUN_SORT.getGunSort(gunLocation);
    }
    public static @NotNull Map<ResourceLocation, @NotNull Integer> getAllGunSort() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.GUN_SORT.getAllGunSort();
        else return SyncDataCache.INSTANCE.GUN_SORT.getAllGunSort();
    }
    public static @Nullable AttachmentIndexInstance getAttachmentIndexInstance(ResourceLocation attachmentLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_INDEX.get(attachmentLocation);
        else return SyncDataCache.INSTANCE.ATTACHMENT_INDEX.get(attachmentLocation);
    }
    public static Set<Map.Entry<ResourceLocation, @NotNull AttachmentIndexInstance>> getAllAttachmentIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.ATTACHMENT_INDEX.entrySet();
    }
    public static @Nullable AmmoIndexInstance getAmmoIndexInstance(ResourceLocation ammoLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.AMMO_INDEX.get(ammoLocation);
        else return SyncDataCache.INSTANCE.AMMO_INDEX.get(ammoLocation);
    }
    public static Set<Map.Entry<ResourceLocation, @NotNull AmmoIndexInstance>> getAllAmmoIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.AMMO_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.AMMO_INDEX.entrySet();
    }
    public static @Nullable BlockIndexInstance getBlockIndexInstance(ResourceLocation blockLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.BLOCK_INDEX.get(blockLocation);
        else return SyncDataCache.INSTANCE.BLOCK_INDEX.get(blockLocation);
    }
    public static Set<Map.Entry<ResourceLocation, @NotNull BlockIndexInstance>> getAllBlockIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.BLOCK_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.BLOCK_INDEX.entrySet();
    }
    public static boolean hasAttachmentInstallability(ResourceLocation attachmentLocation, ResourceLocation gunLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_INSTALLABILITY.hasAttachmentInstallability(attachmentLocation, gunLocation);
        else return SyncDataCache.INSTANCE.ATTACHMENT_INSTALLABILITY.hasAttachmentInstallability(attachmentLocation, gunLocation);
    }
    public static @Nullable ClassUtils.ArraySet<ResourceLocation> getAttachmentInstallability(ResourceLocation attachmentLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_INSTALLABILITY.getAttachmentInstallability(attachmentLocation);
        else return SyncDataCache.INSTANCE.ATTACHMENT_INSTALLABILITY.getAttachmentInstallability(attachmentLocation);
    }
}
