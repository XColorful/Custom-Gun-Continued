/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.init;

import xiao.customgun.CustomGun;
import xiao.customgun.core.api.config.IModConfigSpecBuilder;
import xiao.customgun.core.api.config.ModConfigType;
import xiao.customgun.core.config.AmmoConfig;
import xiao.customgun.core.config.GunConfig;
import xiao.customgun.core.config.OtherConfig;
import xiao.customgun.core.config.SyncConfig;

public class ModConfig {

    public static void init() {
        CommonConfig.init();
        ServerConfig.init();
    }

    private static class CommonConfig {
        public static void init() {
            IModConfigSpecBuilder builder = CustomGun.getModConfigSpecBuilder();
            GunConfig.init(builder);
            AmmoConfig.init(builder);
            OtherConfig.init(builder);
            builder.buildAndRegister(ModConfigType.COMMON);
        }
    }

    private static class ServerConfig {
        public static void init() {
            IModConfigSpecBuilder builder = CustomGun.getModConfigSpecBuilder();
            SyncConfig.init(builder);
            builder.buildAndRegister(ModConfigType.SERVER);
        }
    }
}
