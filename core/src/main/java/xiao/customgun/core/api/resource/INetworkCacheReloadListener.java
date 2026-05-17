/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import xiao.customgun.core.resource.network.SyncDataType;

import java.util.Map;

public interface INetworkCacheReloadListener extends PreparableReloadListener {

    SyncDataType getSyncDataType();

    Map<ResourceLocation, String> getNetworkCache();
}
