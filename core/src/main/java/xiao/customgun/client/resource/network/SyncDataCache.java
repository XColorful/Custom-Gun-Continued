/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.network;

import com.google.gson.stream.JsonReader;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.resource.data.DataFolderType;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.BlockData;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.index.AmmoIndex;
import xiao.customgun.core.resource.data.index.AttachmentIndex;
import xiao.customgun.core.resource.data.index.BlockIndex;
import xiao.customgun.core.resource.data.index.GunIndex;
import xiao.customgun.core.resource.data.modtags.AttachmentTagData;
import xiao.customgun.core.resource.data.modtags.GunAttachmentData;
import xiao.customgun.core.resource.data.recipefilter.RecipeFilterData;
import xiao.customgun.core.resource.network.SyncDataType;
import xiao.customgun.core.util.JsonUtils;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public final class SyncDataCache {
    public static final SyncDataCache INSTANCE = new SyncDataCache();
    private SyncDataCache() {}

    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#DATA}
     */
    public @NotNull volatile Map<Identifier, GunData> gunData = new HashMap<>();
    public @NotNull volatile Map<Identifier, AttachmentData> attachmentData = new HashMap<>();
    public @NotNull volatile Map<Identifier, BlockData> blockData = new HashMap<>();
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#INDEX}
     */
    public @NotNull volatile Map<Identifier, GunIndex> gunIndex = new HashMap<>();
    public @NotNull volatile Map<Identifier, AttachmentIndex> attachmentIndex = new HashMap<>();
    public @NotNull volatile Map<Identifier, AmmoIndex> ammoIndex = new HashMap<>();
    public @NotNull volatile Map<Identifier, BlockIndex> blockIndex = new HashMap<>();
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#RECIPE_FILTER}
     */
    public @NotNull volatile Map<Identifier, RecipeFilterData> recipeFilterData = new HashMap<>();
    /**
     * ./datapacks/{datapack}/data/{namespace}/{@link DataFolderType#MOD_TAG}
     */
    public @NotNull volatile Map<Identifier, AttachmentTagData> attachmentTagData = new HashMap<>();
    public @NotNull volatile Map<Identifier, GunAttachmentData> gunAttachmentData = new HashMap<>();

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
    }

    /**
     * 线程不安全（由主线程或者通过确定无竞态的 Ticket 触发）
     */
    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public void setParseResult(Map<SyncDataType, Map<Identifier, ? extends ResourcePojo<?>>> result) {
        if (result == null) return;

        this.gunData = (Map<Identifier, GunData>) result.getOrDefault(SyncDataType.GUN_DATA, new HashMap<>());
        this.attachmentData = (Map<Identifier, AttachmentData>) result.getOrDefault(SyncDataType.ATTACHMENT_DATA, new HashMap<>());
        this.blockData = (Map<Identifier, BlockData>) result.getOrDefault(SyncDataType.BLOCK_DATA, new HashMap<>());

        this.gunIndex = (Map<Identifier, GunIndex>) result.getOrDefault(SyncDataType.GUN_INDEX, new HashMap<>());
        this.attachmentIndex = (Map<Identifier, AttachmentIndex>) result.getOrDefault(SyncDataType.ATTACHMENT_INDEX, new HashMap<>());
        this.ammoIndex = (Map<Identifier, AmmoIndex>) result.getOrDefault(SyncDataType.AMMO_INDEX, new HashMap<>());
        this.blockIndex = (Map<Identifier, BlockIndex>) result.getOrDefault(SyncDataType.BLOCK_INDEX, new HashMap<>());

        this.recipeFilterData = (Map<Identifier, RecipeFilterData>) result.getOrDefault(SyncDataType.RECIPE_FILTER, new HashMap<>());

        this.attachmentTagData = (Map<Identifier, AttachmentTagData>) result.getOrDefault(SyncDataType.ATTACHMENT_TAG, new HashMap<>());
        this.gunAttachmentData = (Map<Identifier, GunAttachmentData>) result.getOrDefault(SyncDataType.GUN_ATTACHMENT, new HashMap<>());
        // TODO AllowAttachmentTagMatcher.resetCache()
    }
    /**
     * 线程安全
     */
    public Map<SyncDataType, Map<Identifier, ? extends ResourcePojo<?>>> rebuildFromNetworkAsync(Map<SyncDataType, Map<Identifier, String>> cache) {
        Map<SyncDataType, Map<Identifier, ? extends ResourcePojo<?>>> resultMap = new HashMap<>();
        for (Map.Entry<SyncDataType, Map<Identifier, String>> entry : cache.entrySet()) {
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

    private <T extends ResourcePojo<T>> Map<Identifier, T> parseFromNetworkAsync(Map<Identifier, String> cache,
                                                                                       JsonUtils.ReadFunction<T> parser) {
        Map<Identifier, T> map = new HashMap<>();
        for (Map.Entry<Identifier, String> entry : cache.entrySet()) {
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
