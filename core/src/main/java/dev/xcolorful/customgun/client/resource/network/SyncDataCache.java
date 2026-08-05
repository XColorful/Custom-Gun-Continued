/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.network;

import com.google.gson.stream.JsonReader;
import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.api.resource.data.DataFolderType;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.BlockData;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.data.index.BlockIndex;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.data.modtags.AttachmentTagData;
import dev.xcolorful.customgun.core.resource.data.modtags.GunAttachmentData;
import dev.xcolorful.customgun.core.resource.data.recipefilter.RecipeFilterData;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.BlockIndexInstance;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.resource.network.SyncDataType;
import dev.xcolorful.customgun.core.resource.network._AttachmentInstallabilityCache;
import dev.xcolorful.customgun.core.resource.network._GunSortCache;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static dev.xcolorful.customgun.core.resource._DataInstanceManager.buildPojoInstance;

public final class SyncDataCache {
    public static final SyncDataCache INSTANCE = new SyncDataCache();
    private SyncDataCache() {}

    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#DATA}
     */
    public @NotNull volatile Map<ResourceLocation, GunData> gunData = new HashMap<>();
    public @NotNull volatile Map<ResourceLocation, AttachmentData> attachmentData = new HashMap<>();
    public @NotNull volatile Map<ResourceLocation, BlockData> blockData = new HashMap<>();
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#INDEX}
     */
    public @NotNull volatile Map<ResourceLocation, GunIndex> gunIndex = new HashMap<>();
    public @NotNull volatile Map<ResourceLocation, AttachmentIndex> attachmentIndex = new HashMap<>();
    public @NotNull volatile Map<ResourceLocation, AmmoIndex> ammoIndex = new HashMap<>();
    public @NotNull volatile Map<ResourceLocation, BlockIndex> blockIndex = new HashMap<>();
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#RECIPE_FILTER}
     */
    public @NotNull volatile Map<ResourceLocation, RecipeFilterData> recipeFilterData = new HashMap<>();
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#MOD_TAG}
     */
    public @NotNull volatile Map<ResourceLocation, AttachmentTagData> attachmentTagData = new HashMap<>();
    public @NotNull volatile Map<ResourceLocation, GunAttachmentData> gunAttachmentData = new HashMap<>();

    // data instance
    public final Map<ResourceLocation, GunIndexInstance> GUN_INDEX = new HashMap<>();
    public final _GunSortCache GUN_SORT = new _GunSortCache();
    public final Map<ResourceLocation, AttachmentIndexInstance> ATTACHMENT_INDEX = new HashMap<>();
    public final Map<ResourceLocation, AmmoIndexInstance> AMMO_INDEX = new HashMap<>();
    public final Map<ResourceLocation, BlockIndexInstance> BLOCK_INDEX = new HashMap<>();
    public final _AttachmentInstallabilityCache ATTACHMENT_INSTALLABILITY = new _AttachmentInstallabilityCache();

    @ApiStatus.Internal
    public void clear() {
        this.gunData = new HashMap<>();
        this.attachmentData = new HashMap<>();
        this.blockData = new HashMap<>();

        this.gunIndex = new HashMap<>();
        this.attachmentIndex = new HashMap<>();
        this.ammoIndex = new HashMap<>();
        this.blockIndex = new HashMap<>();

        this.recipeFilterData = new HashMap<>();

        this.attachmentTagData = new HashMap<>();
        this.gunAttachmentData = new HashMap<>();

        // data instance
        this.GUN_INDEX.clear();
        this.GUN_SORT.clear();
        this.ATTACHMENT_INDEX.clear();
        this.AMMO_INDEX.clear();
        this.BLOCK_INDEX.clear();
        this.ATTACHMENT_INSTALLABILITY.clear();
    }

    /**
     * 线程不安全（由主线程或者通过确定无竞态的 Ticket 触发）
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public void setParseResult(Map<SyncDataType, Map<ResourceLocation, ? extends ResourcePojo<?>>> result) {
        if (result == null) return;

        this.gunData = (Map<ResourceLocation, GunData>) result.getOrDefault(SyncDataType.GUN_DATA, new HashMap<>());
        this.attachmentData = (Map<ResourceLocation, AttachmentData>) result.getOrDefault(SyncDataType.ATTACHMENT_DATA, new HashMap<>());
        this.blockData = (Map<ResourceLocation, BlockData>) result.getOrDefault(SyncDataType.BLOCK_DATA, new HashMap<>());

        this.gunIndex = (Map<ResourceLocation, GunIndex>) result.getOrDefault(SyncDataType.GUN_INDEX, new HashMap<>());
        this.attachmentIndex = (Map<ResourceLocation, AttachmentIndex>) result.getOrDefault(SyncDataType.ATTACHMENT_INDEX, new HashMap<>());
        this.ammoIndex = (Map<ResourceLocation, AmmoIndex>) result.getOrDefault(SyncDataType.AMMO_INDEX, new HashMap<>());
        this.blockIndex = (Map<ResourceLocation, BlockIndex>) result.getOrDefault(SyncDataType.BLOCK_INDEX, new HashMap<>());

        this.recipeFilterData = (Map<ResourceLocation, RecipeFilterData>) result.getOrDefault(SyncDataType.RECIPE_FILTER, new HashMap<>());

        this.attachmentTagData = (Map<ResourceLocation, AttachmentTagData>) result.getOrDefault(SyncDataType.ATTACHMENT_TAG, new HashMap<>());
        this.gunAttachmentData = (Map<ResourceLocation, GunAttachmentData>) result.getOrDefault(SyncDataType.GUN_ATTACHMENT, new HashMap<>());

        // data instance
        buildPojoInstance(ResourceApi.getAllGunIndex(), this.GUN_INDEX, GunIndexInstance::fromPojo, GunIndexInstance.class);
        this.GUN_SORT.reload();
        buildPojoInstance(ResourceApi.getAllAttachmentIndex(), this.ATTACHMENT_INDEX, AttachmentIndexInstance::fromPojo, AttachmentIndexInstance.class);
        buildPojoInstance(ResourceApi.getAllAmmoIndex(), this.AMMO_INDEX, AmmoIndexInstance::fromPojo, AmmoIndexInstance.class);
        buildPojoInstance(ResourceApi.getAllBlockIndex(), this.BLOCK_INDEX, BlockIndexInstance::fromPojo, BlockIndexInstance.class);
        this.ATTACHMENT_INSTALLABILITY.reload();
    }
    /**
     * 线程安全
     */
    public Map<SyncDataType, Map<ResourceLocation, ? extends ResourcePojo<?>>> rebuildFromNetworkAsync(Map<SyncDataType, Map<ResourceLocation, String>> cache) {
        Map<SyncDataType, Map<ResourceLocation, ? extends ResourcePojo<?>>> resultMap = new HashMap<>();
        for (Map.Entry<SyncDataType, Map<ResourceLocation, String>> entry : cache.entrySet()) {
            switch (entry.getKey()) {
                case GUN_DATA -> resultMap.put(SyncDataType.GUN_DATA, parseFromNetworkAsync(entry.getValue(), GunData::fromJson));
                case ATTACHMENT_DATA -> resultMap.put(SyncDataType.ATTACHMENT_DATA, parseFromNetworkAsync(entry.getValue(), AttachmentData::fromJson));
                case AMMO_INDEX -> resultMap.put(SyncDataType.AMMO_INDEX, parseFromNetworkAsync(entry.getValue(), AmmoIndex::fromJson));
                case GUN_INDEX -> resultMap.put(SyncDataType.GUN_INDEX, parseFromNetworkAsync(entry.getValue(), GunIndex::fromJson));
                case ATTACHMENT_INDEX -> resultMap.put(SyncDataType.ATTACHMENT_INDEX, parseFromNetworkAsync(entry.getValue(), AttachmentIndex::fromJson));
                case RECIPES -> {}
                case RECIPE_FILTER -> resultMap.put(SyncDataType.RECIPE_FILTER, parseFromNetworkAsync(entry.getValue(), RecipeFilterData::fromJson));
                case ATTACHMENT_TAG -> resultMap.put(SyncDataType.ATTACHMENT_TAG, parseFromNetworkAsync(entry.getValue(), AttachmentTagData::fromJson));
                case GUN_ATTACHMENT -> resultMap.put(SyncDataType.GUN_ATTACHMENT, parseFromNetworkAsync(entry.getValue(), GunAttachmentData::fromJson));
                case BLOCK_DATA -> resultMap.put(SyncDataType.BLOCK_DATA, parseFromNetworkAsync(entry.getValue(), BlockData::fromJson));
                case BLOCK_INDEX -> resultMap.put(SyncDataType.BLOCK_INDEX, parseFromNetworkAsync(entry.getValue(), BlockIndex::fromJson));
//                default -> ; // 如果添加了同步类型(枚举)，强制此处编译不通过
            }
        }
        return resultMap;
    }

    private <T extends ResourcePojo<T>> Map<ResourceLocation, T> parseFromNetworkAsync(Map<ResourceLocation, String> cache,
                                                                                       JsonUtils.ReadFunction<T> parser) {
        Map<ResourceLocation, T> map = new HashMap<>();
        for (Map.Entry<ResourceLocation, String> entry : cache.entrySet()) {
            String jsonStr = entry.getValue();
            if (jsonStr == null || jsonStr.isEmpty()) {
                continue;
            }
            try (StringReader stringReader = new StringReader(jsonStr);
                 JsonReader jsonReader = new JsonReader(stringReader)) {

                T pojo = parser.apply(jsonReader);

                if (pojo != null) {
                    pojo.validate();
                    if (pojo.isValid()) map.put(entry.getKey(), pojo);
                    else CustomGun.LOGGER.debug("Received invalid pojo (ResourceLocation: {})", entry.getKey());
                }
            } catch (Exception e) {
                CustomGun.LOGGER.error("Error parsing pojo (ResourceLocation: {})", entry.getKey(), e);
            }
        }
        return map;
    }
}
