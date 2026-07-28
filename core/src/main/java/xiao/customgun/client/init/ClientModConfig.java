/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.init;

import xiao.customgun.CustomGun;
import xiao.customgun.client.config.*;
import xiao.customgun.client.config.sync.InteractFilterData;
import xiao.customgun.core.api.config.IModConfigSpecBuilder;
import xiao.customgun.core.api.config.ModConfigType;
import xiao.customgun.core.config.sync.HeadAABBData;

public class ClientModConfig {

    public static void init(){
        ClientConfig.init();
    }

    private static class ClientConfig {
        public static void init() {
            IModConfigSpecBuilder builder = CustomGun.getModConfigSpecBuilder();
            KeyConfig.init(builder);
            RenderConfig.init(builder);
            ResourceConfig.init(builder);
            SoundConfig.init(builder);
            ZoomConfig.init(builder);
            builder.buildAndRegister(ModConfigType.CLIENT);
        }
    }

    /**
     * 仅逻辑客户端触发
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
                InteractFilterData.reloadInteractFilter();
            }
        }
        public void onReloadingConfig(ModConfigType modConfigType) {
            if (modConfigType == ModConfigType.SERVER) {
                HeadAABBData.reloadHeadAABB();
                InteractFilterData.reloadInteractFilter();
            }
        }
    }
}
