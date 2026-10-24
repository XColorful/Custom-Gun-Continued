package dev.xcolorful.customgun.core.config;

import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;
import dev.xcolorful.customgun.core.api.config.ModConfigTag;

public class ShooterConfig {
    public static IModConfigSpec<Boolean> CREATIVE_MODE_INFINITE_FEED;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ModConfigTag.shooter_path);

        builder.addComment(ModConfigTag.creativeModeInfiniteFeed_comment);
        CREATIVE_MODE_INFINITE_FEED = builder.addConfig(ModConfigTag.creativeModeInfiniteFeed_path, true);

        builder.finishBuild();
    }
}
