/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.config;

import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;
import dev.xcolorful.customgun.core.api.config.ModConfigTag;

public class OtherConfig {
    public static IModConfigSpec<Integer> TARGET_SOUND_DISTANCE;
    public static IModConfigSpec<Double> SERVER_HITBOX_OFFSET;
    public static IModConfigSpec<Boolean> SERVER_HITBOX_LATENCY_FIX;
    public static IModConfigSpec<Double> SERVER_HITBOX_LATENCY_MAX_SAVE_MS;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ModConfigTag.other_path);

        builder.addComment(ModConfigTag.targetSoundDistance_comment);
        TARGET_SOUND_DISTANCE = builder.addConfig(ModConfigTag.targetSoundDistance_path, 128, 0, Integer.MAX_VALUE);

        builder.addComment(ModConfigTag.serverHitboxOffset_comment);
        SERVER_HITBOX_OFFSET = builder.addConfig(ModConfigTag.serverHitboxOffset_path, 3.0, -Double.MAX_VALUE, Double.MAX_VALUE);

        builder.addComment(ModConfigTag.serverHitboxLatencyFix_comment);
        SERVER_HITBOX_LATENCY_FIX = builder.addConfig(ModConfigTag.serverHitboxLatencyFix_path, true);

        builder.addComment(ModConfigTag.serverHitboxLatencyMaxSaveMs_comment);
        SERVER_HITBOX_LATENCY_MAX_SAVE_MS = builder.addConfig(ModConfigTag.serverHitboxLatencyMaxSaveMs_path, 1000.0, 250.0, Double.MAX_VALUE);

        builder.finishBuild();
    }
}