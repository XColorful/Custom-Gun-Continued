/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.config;

import dev.xcolorful.customgun.client.api.config.ClientModConfigTag;
import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;

public class ResourceConfig {
    public static IModConfigSpec<Boolean> ENABLE_LAZY_CLIENT_ASSET_LOAD;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ClientModConfigTag.resource_path);

        builder.addComments(ClientModConfigTag.enableLazyClientAssetLoad_comment);
        ENABLE_LAZY_CLIENT_ASSET_LOAD = builder.addConfig(ClientModConfigTag.enableLazyClientAssetLoad_path, true);

        builder.finishBuild();
    }
}