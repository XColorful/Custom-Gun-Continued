/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.resource.FileExtensionType;
import xiao.customgun.core.api.resource.INetworkCacheReloadListener;
import xiao.customgun.core.api.resource.data.DataFolderName;
import xiao.customgun.core.api.resource.data.DataFolderType;
import xiao.customgun.core.api.resource.data.data.DataSubFolderType;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.ResourcePojoManager;
import xiao.customgun.core.resource.network.SyncDataType;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.BlockData;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.util.JsonUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * 目录名称{@link DataFolderType} + 子目录名称{@link DataSubFolderType}
 */
public abstract class DataManager<T extends ResourcePojo<T>> extends ResourcePojoManager<T> {

    public DataManager(String subPrefix, String extension, JsonUtils.ReadFunction<T> fromJson) {
        super(PackType.SERVER_DATA, Arrays.asList(DataFolderType.DATA.getFolderName() + "/" + subPrefix, DataFolderName.DATA_OLD1 + "/" + subPrefix),
                extension, fromJson);
    }

    public static final class GunDataManager extends DataManager<GunData> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public GunDataManager() {
            super(DataSubFolderType.GUN.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    GunData::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.GUN_DATA;
        }
        @Override public Map<ResourceLocation, String> getNetworkCache() {
            return Map.of();
        }
    }

    public static final class AttachmentDataManager extends DataManager<AttachmentData> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public AttachmentDataManager() {
            super(DataSubFolderType.ATTACHMENT.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    AttachmentData::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.ATTACHMENT_DATA;
        }
        @Override public Map<ResourceLocation, String> getNetworkCache() {
            return Map.of();
        }
    }

    public static final class BlockDataManager extends DataManager<BlockData> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public BlockDataManager() {
            super(DataSubFolderType.BLOCK.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    BlockData::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.BLOCK_DATA;
        }
        @Override public Map<ResourceLocation, String> getNetworkCache() {
            return Map.of();
        }
    }
}
