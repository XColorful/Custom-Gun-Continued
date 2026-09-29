package dev.xcolorful.customgun.client.init;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.config.*;
import dev.xcolorful.customgun.client.config.sync.InteractFilterData;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;
import dev.xcolorful.customgun.core.api.config.ModConfigType;
import dev.xcolorful.customgun.core.config.AmmoConfig;
import dev.xcolorful.customgun.core.config.sync.HeadAABBData;

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
            switch (modConfigType) {
                case SERVER -> {
                    HeadAABBData.reloadHeadAABB();
                    InteractFilterData.reloadInteractFilter();
                }
                case COMMON -> {
                    AmmoConfig.reloadPassThroughBlocks();
                }
            }
        }
        public void onReloadingConfig(ModConfigType modConfigType) {
            switch (modConfigType) {
                case SERVER -> {
                    HeadAABBData.reloadHeadAABB();
                    InteractFilterData.reloadInteractFilter();
                }
                case COMMON -> {
                    AmmoConfig.reloadPassThroughBlocks();
                }
            }
        }
    }
}
