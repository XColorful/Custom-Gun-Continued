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

public class ZoomConfig {
    public static IModConfigSpec<Double> SCREEN_DISTANCE_COEFFICIENT;
    public static IModConfigSpec<Double> ZOOM_SENSITIVITY_BASE_MULTIPLIER;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ClientModConfigTag.zoom_path);

        builder.addComment(ClientModConfigTag.screenDistanceCoefficient_comment);
        SCREEN_DISTANCE_COEFFICIENT = builder.addConfig(ClientModConfigTag.screenDistanceCoefficient_path, 1.33, 0.0, 3.0);

        builder.addComment(ClientModConfigTag.zoomSensitivityBaseMultiplier_comment);
        ZOOM_SENSITIVITY_BASE_MULTIPLIER = builder.addConfig(ClientModConfigTag.zoomSensitivityBaseMultiplier_path, 1.0, 0.0, 2.0);

        builder.finishBuild();
    }
}