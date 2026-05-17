/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data;

import xiao.customgun.core.api.resource.ResourceTag;

/**
 * "./tacz/{枪包}/data/{namespace}/" 下的目录名
 */
public enum DataFolderType implements ResourceTag {
    GUNPACK_META(DataFolderName.GUNPACK_META),
    DATA(DataFolderName.DATA),
    INDEX(DataFolderName.INDEX),
    RECIPE_FILTER(DataFolderName.RECIPE_FILTER),
    /**
     * 原版目录，如果不用原版目录，那得手动处理recipe同步
     */
    @Deprecated RECIPE(DataFolderName.RECIPE),
    SCRIPT(DataFolderName.SCRIPT),
    @Deprecated LOOT_INJECTOR(DataFolderName.LOOT_INJECTOR),
    MOD_TAG(DataFolderName.MOD_TAG);

    public final String folderName;
    DataFolderType(String folderName) {
        this.folderName = folderName;
    }

    @Override public String getTagName() {
        return this.folderName.toLowerCase();
    }

    public String getFolderName() {
        return this.folderName;
    }
}
