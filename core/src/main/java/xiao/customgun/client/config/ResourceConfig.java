/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.config;

import xiao.customgun.client.api.config.ClientModConfigTag;
import xiao.customgun.core.api.config.IModConfigSpec;
import xiao.customgun.core.api.config.IModConfigSpecBuilder;

public class ResourceConfig {
    public static IModConfigSpec<Boolean> ENABLE_LAZY_CLIENT_ASSET_LOAD;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ClientModConfigTag.resource_path);

        builder.addComments(ClientModConfigTag.enableLazyClientAssetLoad_comment);
        ENABLE_LAZY_CLIENT_ASSET_LOAD = builder.addConfig(ClientModConfigTag.enableLazyClientAssetLoad_path, true);

        builder.finishBuild();
    }
}