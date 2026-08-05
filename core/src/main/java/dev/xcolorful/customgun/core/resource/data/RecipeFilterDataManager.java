/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data;

import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.api.resource.INetworkCacheReloadListener;
import dev.xcolorful.customgun.core.api.resource.data.DataFolderName;
import dev.xcolorful.customgun.core.api.resource.data.DataFolderType;
import dev.xcolorful.customgun.core.resource.ResourcePojoManager;
import dev.xcolorful.customgun.core.resource.data.recipefilter.RecipeFilterData;
import dev.xcolorful.customgun.core.resource.network.SyncDataType;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;

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
}