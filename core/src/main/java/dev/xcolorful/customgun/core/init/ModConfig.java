/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;
import dev.xcolorful.customgun.core.api.config.ModConfigType;
import dev.xcolorful.customgun.core.config.AmmoConfig;
import dev.xcolorful.customgun.core.config.GunConfig;
import dev.xcolorful.customgun.core.config.OtherConfig;
import dev.xcolorful.customgun.core.config.SyncConfig;
import dev.xcolorful.customgun.core.config.sync.HeadAABBData;

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

    /**
     * 仅逻辑服务端触发
     */
    public static class Event {
        private static final Event INSTANCE = new Event();
        public static Event get() {
            return INSTANCE;
        }
        private Event() {}

        public void onLoadingConfig(ModConfigType modConfigType) {
            if (modConfigType == ModConfigType.SERVER) {
                HeadAABBData.reloadHeadAABB();
            }
        }
        public void onReloadingConfig(ModConfigType modConfigType) {
            if (modConfigType == ModConfigType.SERVER) {
                HeadAABBData.reloadHeadAABB();
            }
        }
    }
}
