/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data;

import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.resource.ResourceTag;

/**
 * "./tacz/{枪包}/data/{namespace}/" 下的目录名
 */
public enum DataFolderType implements ResourceTag {
    /**
     * 在跟 "assets" 和 "data" 同级的目录，不属于原版类型
     */
    @ApiStatus.Internal @Deprecated GUNPACK_META(DataFolderName.GUNPACK_META),
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
