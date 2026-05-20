/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.resource.network.SyncDataCache;
import xiao.customgun.core.resource.AllDataManager;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.index.GunIndex;

public class ResourceApi {

    public static @Nullable GunIndex getGunIndex(ResourceLocation indexLocation) {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null) return dataManager.gunIndexManager.getPojo(indexLocation);
        else return SyncDataCache.INSTANCE.gunIndex.get(indexLocation);
    }
    public static @Nullable GunData getGunData(ResourceLocation dataLocation) {
        var dataManager = AllDataManager.getCurrent();
        if (dataManager != null) return dataManager.gunDataManager.getPojo(dataLocation);
        else return SyncDataCache.INSTANCE.gunData.get(dataLocation);
    }
}
