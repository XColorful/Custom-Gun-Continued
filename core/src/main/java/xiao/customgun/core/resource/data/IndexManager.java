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
import xiao.customgun.core.api.resource.data.index.IndexSubFolderType;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.ResourcePojoManager;
import xiao.customgun.core.resource.network.SyncDataType;
import xiao.customgun.core.resource.data.index.AmmoIndex;
import xiao.customgun.core.resource.data.index.AttachmentIndex;
import xiao.customgun.core.resource.data.index.BlockIndex;
import xiao.customgun.core.resource.data.index.GunIndex;
import xiao.customgun.core.util.JsonUtils;

import java.util.Arrays;
import java.util.Map;

/**
 * 目录名称{@link DataFolderType} + 子目录名称{@link IndexSubFolderType}
 */
public abstract class IndexManager<T extends ResourcePojo<T>> extends ResourcePojoManager<T> {

    public IndexManager(String subPrefix, String extension, JsonUtils.ReadFunction<T> fromJson) {
        super(PackType.SERVER_DATA, Arrays.asList(DataFolderType.INDEX.getFolderName() + "/" + subPrefix, DataFolderName.INDEX_OLD1 + "/" + subPrefix),
                extension, fromJson);
    }

    public static final class GunIndexManager extends IndexManager<GunIndex> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public GunIndexManager() {
            super(IndexSubFolderType.GUN.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    GunIndex::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.GUN_INDEX;
        }
        @Override public Map<ResourceLocation, String> getNetworkCache() {
            return Map.of();
        }
    }

    public static final class AttachmentIndexManager extends IndexManager<AttachmentIndex> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public AttachmentIndexManager() {
            super(IndexSubFolderType.ATTACHMENT.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    AttachmentIndex::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.ATTACHMENT_INDEX;
        }
        @Override public Map<ResourceLocation, String> getNetworkCache() {
            return Map.of();
        }
    }

    public static final class AmmoIndexManager extends IndexManager<AmmoIndex> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public AmmoIndexManager() {
            super(IndexSubFolderType.AMMO.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    AmmoIndex::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.AMMO_INDEX;
        }
        @Override public Map<ResourceLocation, String> getNetworkCache() {
            return Map.of();
        }
    }

    public static final class BlockIndexManager extends IndexManager<BlockIndex> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public BlockIndexManager() {
            super(IndexSubFolderType.BLOCK.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    BlockIndex::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.BLOCK_INDEX;
        }
        @Override public Map<ResourceLocation, String> getNetworkCache() {
            return Map.of();
        }
    }
}
