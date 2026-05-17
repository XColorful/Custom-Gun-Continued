/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.assets;

import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.client.api.resource.assets.AssetsFolderType;
import xiao.customgun.client.resource.assets.model.BedrockModel;
import xiao.customgun.core.api.resource.FileExtensionType;
import xiao.customgun.core.api.resource.assets.AssetsFolderName;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.ResourcePojoManager;
import xiao.customgun.core.util.JsonUtils;

import java.util.Arrays;

/**
 * 目录名称{@link AssetsFolderType}
 */
public abstract class ModelManager<T extends ResourcePojo<T>> extends ResourcePojoManager<T> {

    public ModelManager(String subPrefix, String extension, JsonUtils.ReadFunction<T> fromJson) {
        super(PackType.CLIENT_RESOURCES, Arrays.asList(AssetsFolderType.MODEL.getFolderName() + "/" + subPrefix, AssetsFolderName.MODEL_OLD1 + "/" + subPrefix),
                extension, fromJson);
    }
    public ModelManager(String extension, JsonUtils.ReadFunction<T> fromJson) {
        super(PackType.CLIENT_RESOURCES, Arrays.asList(AssetsFolderType.MODEL.getFolderName(), AssetsFolderName.MODEL_OLD1),
                extension, fromJson);
    }

    public static final class BedrockModelManager extends ModelManager<BedrockModel> {
        @ApiStatus.Internal
        public BedrockModelManager() {
            super(FileExtensionType.JSON.getExtensionNameWithDot(),
                    BedrockModel::fromJson);
        }
    }
}
