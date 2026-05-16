/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.resource;

import net.minecraft.server.packs.PackType;
import xiao.customgun.core.api.resource.FileExtensionType;
import xiao.customgun.core.api.resource.data.DataFolderType;

/**
 * 实际上不属于原版资源包体系
 * 但保留了相应的 {@link DataFolderType} 以便以后修改标准
 */
public class GunpackMetaManager extends ResourcePojoManager<GunpackMeta> {
    public static final GunpackMetaManager INSTANCE = new GunpackMetaManager();
    public static GunpackMetaManager get() {
        return INSTANCE;
    }
    private GunpackMetaManager() {
        super(PackType.SERVER_DATA, "",
                FileExtensionType.GUNPACK_META.getExtensionNameWithDot(),
                GunpackMeta::fromJson);
    }
}