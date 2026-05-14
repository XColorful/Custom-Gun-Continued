/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource.data;

import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.resource.FileExtensionType;
import xiao.customgun.core.api.resource.data.DataFolderType;
import xiao.customgun.core.init.registry.ModRecipe;
import xiao.customgun.core.resource.ResourcePojoManager;
import xiao.customgun.core.resource.data.recipe.RecipeData;

/**
 * 目录名称{@link DataFolderType}
 * @deprecated 目前是由原版解析recipe目录，在{@link ModRecipe}注册来添加recipe解析格式，所以不集中持有pojo
 */
@Deprecated(forRemoval = false)
public class RecipeDataManager extends ResourcePojoManager<RecipeData> {
    @ApiStatus.Internal
    public RecipeDataManager() {
        super(DataFolderType.RECIPE.getFolderName(),
                FileExtensionType.JSON.getExtensionNameWithDot(),
                RecipeData::fromJson);
    }
}
