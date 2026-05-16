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
import xiao.customgun.core.api.resource.data.modtag.ModTagSubFolderType;
import xiao.customgun.core.api.resource.data.modtag.ModTagSubFolderTypeTag;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.ResourcePojoManager;
import xiao.customgun.core.resource.SyncDataType;
import xiao.customgun.core.resource.data.modtags.AttachmentTagData;
import xiao.customgun.core.resource.data.modtags.GunAttachmentData;
import xiao.customgun.core.util.JsonUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 目录名称{@link DataFolderType} + 子目录名称{@link ModTagSubFolderType}
 */
public abstract class ModTagManager <T extends ResourcePojo<T>> extends ResourcePojoManager<T> {

    public ModTagManager(String subPrefix, String extension, JsonUtils.ReadFunction<T> fromJson) {
        super(PackType.SERVER_DATA, Arrays.asList(DataFolderType.MOD_TAG.getFolderName() + "/" + subPrefix, DataFolderName.MOD_TAGS_OLD1 + "/" + subPrefix),
                extension, fromJson);
    }
    public ModTagManager(List<String> subPrefixList, String extension, JsonUtils.ReadFunction<T> fromJson) {
        this(PackType.SERVER_DATA, subPrefixList.stream()
                .flatMap(subPrefix -> Stream.of(DataFolderType.MOD_TAG.getFolderName() + "/" + subPrefix, DataFolderName.MOD_TAGS_OLD1 + "/" + subPrefix)
                ).toList(),
                extension, fromJson);
    }
    public ModTagManager(PackType packType, List<String> prefixList, String extension, JsonUtils.ReadFunction<T> fromJson) {
        super(packType, prefixList, extension, fromJson);
    }

    public static class AttachmentTagDataManager extends ModTagManager<AttachmentTagData> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public AttachmentTagDataManager() {
            super(Arrays.asList(ModTagSubFolderType.ATTACHMENT_TAG.getFolderName(), ModTagSubFolderTypeTag.ATTACHMENT_TAG_OLD1),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    AttachmentTagData::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.ATTACHMENT_TAG;
        }
        @Override public Map<ResourceLocation, String> getNetworkCache() {
            return Map.of();
        }

        @Override
        protected boolean isPojoLocationValid(ResourceLocation pojoLocation) {
            return !pojoLocation.getPath().startsWith("allow_attachments/"); // 不已旧前缀开头
        }
    }

    public static class GunAttachmentDataManager extends ModTagManager<GunAttachmentData> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public GunAttachmentDataManager() {
            super(Arrays.asList(ModTagSubFolderType.GUN_ATTACHMENT.getFolderName(), ModTagSubFolderTypeTag.GUN_ATTACHMENT_OLD1),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    GunAttachmentData::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.GUN_ATTACHMENT;
        }
        @Override public Map<ResourceLocation, String> getNetworkCache() {
            return Map.of();
        }
    }
}
