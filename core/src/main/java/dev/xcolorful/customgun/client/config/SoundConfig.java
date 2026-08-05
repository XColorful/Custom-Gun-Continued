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

public class SoundConfig {
    public static IModConfigSpec<Integer> HIT_SOUND_CONCURRENCY_LIMIT;
    public static IModConfigSpec<Integer> DEFAULT_SOUND_CONCURRENCY_LIMIT;
    public static IModConfigSpec<Integer> HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT;
    public static IModConfigSpec<Boolean> FIRST_PERSON_ANIMATION_SOUND_TRACKING;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ClientModConfigTag.sound_path);

        builder.addComment(ClientModConfigTag.hitSoundConcurrencyLimit_comment);
        HIT_SOUND_CONCURRENCY_LIMIT = builder.addConfig(ClientModConfigTag.hitSoundConcurrencyLimit_path, 1, 0, 128);

        builder.addComment(ClientModConfigTag.defaultSoundConcurrencyLimit_comment);
        DEFAULT_SOUND_CONCURRENCY_LIMIT = builder.addConfig(ClientModConfigTag.defaultSoundConcurrencyLimit_path, 2, 0, 128);

        builder.addComment(ClientModConfigTag.highFrequencySoundConcurrencyLimit_comment);
        HIGH_FREQUENCY_SOUND_CONCURRENCY_LIMIT = builder.addConfig(ClientModConfigTag.highFrequencySoundConcurrencyLimit_path, 4, 0, 128);

        builder.addComment(ClientModConfigTag.firstPersonAnimationSoundTracking_comment);
        FIRST_PERSON_ANIMATION_SOUND_TRACKING = builder.addConfig(ClientModConfigTag.firstPersonAnimationSoundTracking_path, false);

        builder.finishBuild();
    }
}
