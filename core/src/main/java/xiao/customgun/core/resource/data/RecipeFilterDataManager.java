/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.resource.FileExtensionType;
import xiao.customgun.core.api.resource.INetworkCacheReloadListener;
import xiao.customgun.core.api.resource.data.DataFolderType;
import xiao.customgun.core.resource.ResourcePojoManager;
import xiao.customgun.core.resource.SyncDataType;
import xiao.customgun.core.resource.data.recipefilter.RecipeFilterData;

import java.util.Map;

/**
 * 目录名称{@link DataFolderType}
 */
public class RecipeFilterDataManager extends ResourcePojoManager<RecipeFilterData> implements INetworkCacheReloadListener {
    @ApiStatus.Internal
    public RecipeFilterDataManager() {
        super(DataFolderType.RECIPE_FILTER.getFolderName(),
                FileExtensionType.JSON.getExtensionNameWithDot(),
                RecipeFilterData::fromJson);
    }
    @Override public SyncDataType getSyncDataType() {
        return SyncDataType.RECIPE_FILTER;
    }
    @Override public Map<Identifier, String> getNetworkCache() {
        return Map.of();
    }
}