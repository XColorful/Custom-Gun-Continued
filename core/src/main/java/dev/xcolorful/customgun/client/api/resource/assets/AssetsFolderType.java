/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.resource.assets;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.api.resource.assets.AssetsFolderName;

/**
 * "./resourcepacks/{枪包}/assets/{namespace}/" 下的目录名
 */
public enum AssetsFolderType implements ResourceTag {
    GUNPACK_INFO(AssetsFolderName.GUNPACK_INFO),
    ANIMATIONS(AssetsFolderName.ANIMATIONS),
    DISPLAY(AssetsFolderName.DISPLAY),
    MODEL(AssetsFolderName.MODEL),
    /**
     * 原版目录
     */
    LANG(AssetsFolderName.LANG),
    /**
     * PlayerAnimator目录
     */
    PLAYER_ANIMATOR(AssetsFolderName.PLAYER_ANIMATOR),
    SCRIPT(AssetsFolderName.SCRIPT),
    /**
     * 原版目录
     */
    @Deprecated SOUNDS(AssetsFolderName.SOUNDS),
    /**
     * mixin将文件夹添加到原版扫描的目录
     */
    MOD_SOUNDS(AssetsFolderName.MOD_SOUNDS),
    /**
     * 原版目录
     */
    TEXTURES(AssetsFolderName.TEXTURES);

    public final String folderName;
    AssetsFolderType(String folderName) {
        this.folderName = folderName;
    }

    @Override public String getTagName() {
        return this.folderName.toLowerCase();
    }

    public String getFolderName() {
        return this.folderName;
    }
}
