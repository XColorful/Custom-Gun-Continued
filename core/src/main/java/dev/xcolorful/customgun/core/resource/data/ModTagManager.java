/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data;

import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.api.resource.INetworkCacheReloadListener;
import dev.xcolorful.customgun.core.api.resource.data.DataFolderName;
import dev.xcolorful.customgun.core.api.resource.data.DataFolderType;
import dev.xcolorful.customgun.core.api.resource.data.modtag.ModTagSubFolderType;
import dev.xcolorful.customgun.core.api.resource.data.modtag.ModTagSubFolderTypeTag;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.ResourcePojoManager;
import dev.xcolorful.customgun.core.resource.data.modtags.AttachmentTagData;
import dev.xcolorful.customgun.core.resource.data.modtags.GunAttachmentData;
import dev.xcolorful.customgun.core.resource.network.SyncDataType;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.List;
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

    public static final class AttachmentTagDataManager extends ModTagManager<AttachmentTagData> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public AttachmentTagDataManager() {
            super(Arrays.asList(ModTagSubFolderType.ATTACHMENT_TAG.getFolderName(), ModTagSubFolderTypeTag.ATTACHMENT_TAG_OLD1),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    AttachmentTagData::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.ATTACHMENT_TAG;
        }

        @Override
        protected boolean isPojoLocationValid(Identifier pojoLocation) {
            return !pojoLocation.getPath().startsWith("allow_attachments/"); // 不已旧前缀开头
        }
    }

    public static final class GunAttachmentDataManager extends ModTagManager<GunAttachmentData> implements INetworkCacheReloadListener {
        @ApiStatus.Internal
        public GunAttachmentDataManager() {
            super(Arrays.asList(ModTagSubFolderType.GUN_ATTACHMENT.getFolderName(), ModTagSubFolderTypeTag.GUN_ATTACHMENT_OLD1),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    GunAttachmentData::fromJson);
        }
        @Override public SyncDataType getSyncDataType() {
            return SyncDataType.GUN_ATTACHMENT;
        }
    }
}
