/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets;

import dev.xcolorful.customgun.client.api.resource.assets.AssetsFolderType;
import dev.xcolorful.customgun.client.api.resource.assets.display.DisplaySubFolderType;
import dev.xcolorful.customgun.client.resource.assets.display.AmmoDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.BlockDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.api.resource.assets.AssetsFolderName;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.ResourcePojoManager;
import dev.xcolorful.customgun.core.util.JsonUtils;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

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
