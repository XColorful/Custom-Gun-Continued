/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.resource.network;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.index.GunIndex;

import java.util.HashMap;
import java.util.Map;

public final class SyncDataCache {
    public static final SyncDataCache INSTANCE = new SyncDataCache();
    private SyncDataCache() {}

    public Map<ResourceLocation, GunIndex> gunIndex = new HashMap<>();
    public Map<ResourceLocation, GunData> gunData = new HashMap<>();

    public @Nullable GunIndex getGunIndex(ResourceLocation gunLocation) {
        return gunIndex.get(gunLocation);
    }
    public @Nullable GunData getGunData(ResourceLocation gunLocation) {
        return gunData.get(gunLocation);
    }
}
