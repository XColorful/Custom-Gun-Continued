package dev.xcolorful.customgun.client.config;

import dev.xcolorful.customgun.core.api.config.ClientModConfigTag;
import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;

public class ZoomConfig {
    public static IModConfigSpec<Boolean> ENABLE_SCROLLING_ZOOM;
    public static IModConfigSpec<Boolean> SCROLLING_ZOOM_CYCLE;
    public static IModConfigSpec<Boolean> INVERT_SCROLLING_ZOOM;
    public static IModConfigSpec<Double> SCREEN_DISTANCE_COEFFICIENT;
    public static IModConfigSpec<Double> ZOOM_SENSITIVITY_BASE_MULTIPLIER;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ClientModConfigTag.zoom_path);

        builder.addComment(ClientModConfigTag.enableScrollingZoom_comment);
        ENABLE_SCROLLING_ZOOM = builder.addConfig(ClientModConfigTag.enableScrollingZoom_path, true);

        builder.addComment(ClientModConfigTag.scrollingZoomCycle_comment);
        SCROLLING_ZOOM_CYCLE = builder.addConfig(ClientModConfigTag.scrollingZoomCycle_path, false);

        builder.addComment(ClientModConfigTag.invertScrollingZoom_comment);
        INVERT_SCROLLING_ZOOM = builder.addConfig(ClientModConfigTag.invertScrollingZoom_path, true);

        builder.addComment(ClientModConfigTag.screenDistanceCoefficient_comment);
        SCREEN_DISTANCE_COEFFICIENT = builder.addConfig(ClientModConfigTag.screenDistanceCoefficient_path, 1.33, 0.0, 3.0);

        builder.addComment(ClientModConfigTag.zoomSensitivityBaseMultiplier_comment);
        ZOOM_SENSITIVITY_BASE_MULTIPLIER = builder.addConfig(ClientModConfigTag.zoomSensitivityBaseMultiplier_path, 1.0, 0.0, 2.0);

        builder.finishBuild();
    }
}