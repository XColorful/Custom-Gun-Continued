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
import xiao.customgun.client.api.resource.assets.display.DisplaySubFolderType;
import xiao.customgun.client.resource.assets.display.AmmoDisplay;
import xiao.customgun.client.resource.assets.display.AttachmentDisplay;
import xiao.customgun.client.resource.assets.display.BlockDisplay;
import xiao.customgun.client.resource.assets.display.GunDisplay;
import xiao.customgun.core.api.resource.FileExtensionType;
import xiao.customgun.core.api.resource.assets.AssetsFolderName;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.ResourcePojoManager;
import xiao.customgun.core.util.JsonUtils;

import java.util.Arrays;

/**
 * 目录名称{@link AssetsFolderType} + 子目录名称{@link DisplaySubFolderType}
 */
public abstract class DisplayManager<T extends ResourcePojo<T>> extends ResourcePojoManager<T> {

    public DisplayManager(String subPrefix, String extension, JsonUtils.ReadFunction<T> fromJson) {
        super(PackType.CLIENT_RESOURCES, Arrays.asList(AssetsFolderType.DISPLAY.getFolderName() + "/" + subPrefix, AssetsFolderName.DISPLAY_OLD1 + "/" + subPrefix),
                extension, fromJson);
    }

    public static final class GunDisplayManager extends DisplayManager<GunDisplay> {
        @ApiStatus.Internal
        public GunDisplayManager() {
            super(DisplaySubFolderType.GUN.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    GunDisplay::fromJson);
        }
    }

    public static final class AttachmentDisplayManager extends DisplayManager<AttachmentDisplay> {
        @ApiStatus.Internal
        public AttachmentDisplayManager() {
            super(DisplaySubFolderType.ATTACHMENT.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    AttachmentDisplay::fromJson);
        }
    }

    public static final class AmmoDisplayManager extends DisplayManager<AmmoDisplay> {
        @ApiStatus.Internal
        public AmmoDisplayManager() {
            super(DisplaySubFolderType.AMMO.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    AmmoDisplay::fromJson);
        }
    }

    public static final class BlockDisplayManager extends DisplayManager<BlockDisplay> {
        @ApiStatus.Internal
        public BlockDisplayManager() {
            super(DisplaySubFolderType.BLOCK.getFolderName(),
                    FileExtensionType.JSON.getExtensionNameWithDot(),
                    BlockDisplay::fromJson);
        }
    }
}
