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

public class KeyConfig {
    public static IModConfigSpec<Boolean> HOLD_TO_AIM;
    public static IModConfigSpec<Boolean> HOLD_TO_CRAWL;
    public static IModConfigSpec<Boolean> AUTO_RELOAD;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ClientModConfigTag.key_path);

        builder.addComment(ClientModConfigTag.holdToAim_comment);
        HOLD_TO_AIM = builder.addConfig(ClientModConfigTag.holdToAim_path, true);

        builder.addComment(ClientModConfigTag.holdToCrawl_comment);
        HOLD_TO_CRAWL = builder.addConfig(ClientModConfigTag.holdToCrawl_path, true);

        builder.addComment(ClientModConfigTag.autoReload_comment);
        AUTO_RELOAD = builder.addConfig(ClientModConfigTag.autoReload_path, false);

        builder.finishBuild();
    }
}