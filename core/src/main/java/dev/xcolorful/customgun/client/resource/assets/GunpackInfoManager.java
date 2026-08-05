/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.resource.assets;

import dev.xcolorful.customgun.client.api.resource.assets.AssetsFolderType;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.resource.FileExtensionType;
import dev.xcolorful.customgun.core.resource.ResourcePojoManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

/**
 * 实际上不属于原版资源包体系
 * 但保留了相应的 {@link AssetsFolderType} 以便以后修改标准
 */
public final class GunpackInfoManager extends ResourcePojoManager<GunpackInfo> {
    @ApiStatus.Internal
    public GunpackInfoManager() {
        super(PackType.CLIENT_RESOURCES, AssetsFolderType.GUNPACK_INFO.getFolderName(),
                FileExtensionType.JSON.getExtensionNameWithDot(),
                GunpackInfo::fromJson);
    }

    @Override
    protected boolean isPojoLocationValid(Identifier pojoLocation) {
        return pojoLocation.getPath().equals("gunpack_info"); // 单个namespace下唯一
    }
}
