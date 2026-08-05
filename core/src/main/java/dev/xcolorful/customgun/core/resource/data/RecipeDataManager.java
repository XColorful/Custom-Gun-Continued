/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data;

import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.api.resource.data.DataFolderType;
import dev.xcolorful.customgun.core.init.registry.ModRecipe;
import dev.xcolorful.customgun.core.resource.ResourcePojoManager;
import dev.xcolorful.customgun.core.resource.data.recipe.RecipeData;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

/**
 * 目录名称{@link DataFolderType}
 * @deprecated 目前是由原版解析recipe目录，在{@link ModRecipe}注册来添加recipe解析格式，所以不集中持有pojo
 */
@Deprecated(forRemoval = false)
public final class RecipeDataManager extends ResourcePojoManager<RecipeData> {
    @ApiStatus.Internal
    public RecipeDataManager() {
        super(PackType.SERVER_DATA, DataFolderType.RECIPE.getFolderName(),
                FileExtensionType.JSON.getExtensionNameWithDot(),
                RecipeData::fromJson);
    }
}
