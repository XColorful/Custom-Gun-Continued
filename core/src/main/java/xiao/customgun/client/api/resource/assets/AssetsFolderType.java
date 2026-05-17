/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.resource.assets;

import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.resource.assets.AssetsFolderName;

/**
 * "./tacz/{枪包}/assets/{namespace}/" 下的目录名
 */
public enum AssetsFolderType implements ResourceTag {
    GUNPACK_INFO(AssetsFolderName.GUNPACK_INFO),
    ANIMATIONS(AssetsFolderName.ANIMATIONS),
    DISPLAY(AssetsFolderName.DISPLAY),
    GEO_MODELS(AssetsFolderName.GEO_MODELS),
    /**
     * 原版目录
     */
    LANG(AssetsFolderName.LANG),
    PLAYER_ANIMATOR(AssetsFolderName.PLAYER_ANIMATOR),
    SCRIPTS(AssetsFolderName.SCRIPTS),
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
