/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.resource.data;

import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.api.resource.data.DataFolderType;
import dev.xcolorful.customgun.core.resource.ResourcePojoManager;
import dev.xcolorful.customgun.core.resource.data.meta.GunpackMeta;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

/**
 * 实际上不属于原版资源包体系
 * 但保留了相应的 {@link DataFolderType} 以便以后修改标准
 */
public final class GunpackMetaManager extends ResourcePojoManager<GunpackMeta> {
    @ApiStatus.Internal
    public GunpackMetaManager() {
        super(PackType.SERVER_DATA, DataFolderType.GUNPACK_META.getFolderName(),
                FileExtensionType.GUNPACK_META.getExtensionNameWithDot(),
                GunpackMeta::fromJson);
    }
}