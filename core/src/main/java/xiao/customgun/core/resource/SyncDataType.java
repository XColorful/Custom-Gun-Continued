/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource;

import xiao.customgun.core.api.resource.data.DataFolderType;

/**
 * 需要同步到客户端的数据类型
 */
public enum SyncDataType {
    GUN_DATA(DataFolderType.DATA),
    ATTACHMENT_DATA(DataFolderType.DATA),
    AMMO_INDEX(DataFolderType.INDEX),
    GUN_INDEX(DataFolderType.INDEX),
    ATTACHMENT_INDEX(DataFolderType.INDEX),
    /**
     * 原版recipe，不需要手动同步
     */
    RECIPES(DataFolderType.RECIPE),
    RECIPE_FILTER(DataFolderType.RECIPE_FILTER),
    ATTACHMENT_TAGS(DataFolderType.TACZ_TAGS),
    ALLOW_ATTACHMENT_TAGS(DataFolderType.TACZ_TAGS),
    BLOCK_DATA(DataFolderType.DATA),
    BLOCK_INDEX(DataFolderType.INDEX);

    public final DataFolderType dataFolderType;
    SyncDataType(DataFolderType dataFolderType) {
        this.dataFolderType = dataFolderType;
    }
}
