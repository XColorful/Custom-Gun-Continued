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
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.recipe.TableRecipe;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
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
import dev.xcolorful.customgun.core.resource.instance.PojoInstance;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.BlockIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.ClassUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 资源API
 * <ul>
 *     <li>正常使用请获取返回{@link PojoInstance}的接口</li>
 *     <li>返回{@link ResourcePojo}的接口仅有Pojo自身的validation</li>
 * </ul>
 */
public class ResourceApi {

    // --------data--------

    public static @Nullable GunData getGunData(Identifier dataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunDataManager != null) return dataManager.gunDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.gunData.get(dataLocation);
    }
    public static Set<Map.Entry<Identifier, GunData>> getAllGunData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunDataManager != null) return dataManager.gunDataManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.gunData.entrySet();
    }
    public static @Nullable AttachmentData getAttachmentData(Identifier dataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentDataManager != null) return dataManager.attachmentDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.attachmentData.get(dataLocation);
    }
    public static Set<Map.Entry<Identifier, AttachmentData>> getAllAttachmentData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentDataManager != null) return dataManager.attachmentDataManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.attachmentData.entrySet();
    }
    public static @Nullable BlockData getBlockData(Identifier dataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockDataManager != null) return dataManager.blockDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.blockData.get(dataLocation);
    }
    public static Set<Map.Entry<Identifier, BlockData>> getAllBlockData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockDataManager != null) return dataManager.blockDataManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.blockData.entrySet();
    }

    // --------index--------

    public static @Nullable GunIndex getGunIndex(Identifier gunLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunIndexManager != null) return dataManager.gunIndexManager.getPojo(gunLocation);
        else return SyncDataCache.INSTANCE.gunIndex.get(gunLocation);
    }
    public static Set<Map.Entry<Identifier, GunIndex>> getAllGunIndex() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunIndexManager != null) return dataManager.gunIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.gunIndex.entrySet();
    }
    public static @Nullable AttachmentIndex getAttachmentIndex(Identifier attachmentLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentIndexManager != null) return dataManager.attachmentIndexManager.getPojo(attachmentLocation);
        else return SyncDataCache.INSTANCE.attachmentIndex.get(attachmentLocation);
    }
    public static Set<Map.Entry<Identifier, AttachmentIndex>> getAllAttachmentIndex() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentIndexManager != null) return dataManager.attachmentIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.attachmentIndex.entrySet();
    }
    public static @Nullable AmmoIndex getAmmoIndex(Identifier ammoLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.ammoIndexManager != null) return dataManager.ammoIndexManager.getPojo(ammoLocation);
        else return SyncDataCache.INSTANCE.ammoIndex.get(ammoLocation);
    }
    public static Set<Map.Entry<Identifier, AmmoIndex>> getAllAmmoIndex() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.ammoIndexManager != null) return dataManager.ammoIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.ammoIndex.entrySet();
    }
    public static @Nullable BlockIndex getBlockIndex(Identifier blockLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockIndexManager != null) return dataManager.blockIndexManager.getPojo(blockLocation);
        else return SyncDataCache.INSTANCE.blockIndex.get(blockLocation);
    }
    public static Set<Map.Entry<Identifier, BlockIndex>> getAllBlockIndex() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.blockIndexManager != null) return dataManager.blockIndexManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.blockIndex.entrySet();
    }

    // --------meta--------

    public static @Nullable GunpackMeta getGunpackMeta(Identifier gunpackMetaLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunpackMetaManager != null) return dataManager.gunpackMetaManager.getPojo(gunpackMetaLocation);
        else return null;
    }
    public static Set<Map.Entry<Identifier, GunpackMeta>> getAllGunpackMeta() {
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
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.recipeFilterDataManager != null) return dataManager.recipeFilterDataManager.getPojo(filterLocation);
        else return SyncDataCache.INSTANCE.recipeFilterData.get(filterLocation);
    }
    public static Set<Map.Entry<Identifier, RecipeFilterData>> getAllRecipeFilterData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.recipeFilterDataManager != null) return dataManager.recipeFilterDataManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.recipeFilterData.entrySet();
    }

    // --------script--------

    public static @Nullable DataScript getDataScript(Identifier scriptLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.scriptManager != null) return dataManager.scriptManager.getFile(scriptLocation);
        else return null;
    }
    public static Set<Map.Entry<Identifier, DataScript>> getAllDataScript() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.scriptManager != null) return dataManager.scriptManager.getAllFiles().entrySet();
        else return new HashSet<>();
    }

    // --------modtags--------

    public static @Nullable AttachmentTagData getAttachmentTagData(Identifier attachmentTagDataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentTagManager != null) return dataManager.attachmentTagManager.getPojo(attachmentTagDataLocation);
        else return SyncDataCache.INSTANCE.attachmentTagData.get(attachmentTagDataLocation);
    }
    public static Set<Map.Entry<Identifier, AttachmentTagData>> getAllAttachmentTagData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.attachmentTagManager != null) return dataManager.attachmentTagManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.attachmentTagData.entrySet();
    }
    public static @Nullable GunAttachmentData getGunAttachmentData(Identifier gunAttachmentDataLocation) {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunAttachmentDataManager != null) return dataManager.gunAttachmentDataManager.getPojo(gunAttachmentDataLocation);
        else return SyncDataCache.INSTANCE.gunAttachmentData.get(gunAttachmentDataLocation);
    }
    public static Set<Map.Entry<Identifier, GunAttachmentData>> getAllGunAttachmentData() {
        var dataManager = _AllDataManager.getCurrent();
        if (dataManager != null && dataManager.gunAttachmentDataManager != null) return dataManager.gunAttachmentDataManager.getAllPojo().entrySet();
        else return SyncDataCache.INSTANCE.gunAttachmentData.entrySet();
    }

    // --------data instance--------

    public static @Nullable GunIndexInstance getGunIndexInstance(Identifier gunLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.GUN_INDEX.get(gunLocation);
        else return SyncDataCache.INSTANCE.GUN_INDEX.get(gunLocation);
    }
    public static Set<Map.Entry<Identifier, @NotNull GunIndexInstance>> getAllGunIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.GUN_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.GUN_INDEX.entrySet();
    }
    public static @Nullable Integer getGunSort(Identifier gunLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.GUN_SORT.getGunSort(gunLocation);
        else return SyncDataCache.INSTANCE.GUN_SORT.getGunSort(gunLocation);
    }
    public static @NotNull Map<Identifier, @NotNull Integer> getAllGunSort() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.GUN_SORT.getAllGunSort();
        else return SyncDataCache.INSTANCE.GUN_SORT.getAllGunSort();
    }
    public static @Nullable AttachmentIndexInstance getAttachmentIndexInstance(Identifier attachmentLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_INDEX.get(attachmentLocation);
        else return SyncDataCache.INSTANCE.ATTACHMENT_INDEX.get(attachmentLocation);
    }
    public static Set<Map.Entry<Identifier, @NotNull AttachmentIndexInstance>> getAllAttachmentIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.ATTACHMENT_INDEX.entrySet();
    }
    public static @Nullable AmmoIndexInstance getAmmoIndexInstance(Identifier ammoLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.AMMO_INDEX.get(ammoLocation);
        else return SyncDataCache.INSTANCE.AMMO_INDEX.get(ammoLocation);
    }
    public static Set<Map.Entry<Identifier, @NotNull AmmoIndexInstance>> getAllAmmoIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.AMMO_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.AMMO_INDEX.entrySet();
    }
    public static @Nullable BlockIndexInstance getBlockIndexInstance(Identifier blockLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.BLOCK_INDEX.get(blockLocation);
        else return SyncDataCache.INSTANCE.BLOCK_INDEX.get(blockLocation);
    }
    public static Set<Map.Entry<Identifier, @NotNull BlockIndexInstance>> getAllBlockIndexInstance() {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.BLOCK_INDEX.entrySet();
        else return SyncDataCache.INSTANCE.BLOCK_INDEX.entrySet();
    }
    public static boolean hasAttachmentInstallability(Identifier attachmentLocation, Identifier gunLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_INSTALLABILITY.hasAttachmentInstallability(attachmentLocation, gunLocation);
        else return SyncDataCache.INSTANCE.ATTACHMENT_INSTALLABILITY.hasAttachmentInstallability(attachmentLocation, gunLocation);
    }
    public static @Nullable ClassUtils.ArraySet<Identifier> getAttachmentInstallability(Identifier attachmentLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_INSTALLABILITY.getAttachmentInstallability(attachmentLocation);
        else return SyncDataCache.INSTANCE.ATTACHMENT_INSTALLABILITY.getAttachmentInstallability(attachmentLocation);
    }
    public static @Nullable Map<AttachmentModifierType, Object> getAttachmentModifiers(Identifier attachmentLocation) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) return _DataInstanceManager.ATTACHMENT_MODIFIER.getModifiers(attachmentLocation);
        else return SyncDataCache.INSTANCE.ATTACHMENT_MODIFIER.getModifiers(attachmentLocation);
    }
}
