/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.resource.FileExtensionType;
import xiao.customgun.core.api.resource.INetworkCacheReloadListener;
import xiao.customgun.core.api.resource.data.DataFolderName;
import xiao.customgun.core.api.resource.data.DataFolderType;
import xiao.customgun.core.resource.ResourcePojoManager;
import xiao.customgun.core.resource.data.recipefilter.RecipeFilterData;
import xiao.customgun.core.resource.network.SyncDataType;

import java.util.Arrays;
import java.util.Map;

/**
 * 目录名称{@link DataFolderType}
 */
public final class RecipeFilterDataManager extends ResourcePojoManager<RecipeFilterData> implements INetworkCacheReloadListener {
    @ApiStatus.Internal
    public RecipeFilterDataManager() {
        super(PackType.SERVER_DATA, Arrays.asList(DataFolderType.RECIPE_FILTER.getFolderName(), DataFolderName.RECIPE_FILTER_OLD1),
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