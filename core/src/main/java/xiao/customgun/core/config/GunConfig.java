/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.config;

import xiao.customgun.core.api.config.IModConfigSpec;
import xiao.customgun.core.api.config.IModConfigSpecBuilder;
import xiao.customgun.core.api.config.ModConfigTag;

public class GunConfig {
    public static IModConfigSpec<Integer> DEFAULT_GUN_FIRE_SOUND_DISTANCE;
    public static IModConfigSpec<Integer> DEFAULT_GUN_SILENCE_SOUND_DISTANCE;
    public static IModConfigSpec<Integer> DEFAULT_GUN_OTHER_SOUND_DISTANCE;
    public static IModConfigSpec<Boolean> CREATIVE_PLAYER_CONSUME_AMMO;
    public static IModConfigSpec<Boolean> AUTO_RELOAD_WHEN_RESPAWN;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ModConfigTag.gun_path);

        builder.addComment(ModConfigTag.defaultGunFireSoundDistance_comment);
        DEFAULT_GUN_FIRE_SOUND_DISTANCE = builder.addConfig(ModConfigTag.defaultGunFireSoundDistance_path, 64, 0, Integer.MAX_VALUE);

        builder.addComment(ModConfigTag.defaultGunSilenceSoundDistance_comment);
        DEFAULT_GUN_SILENCE_SOUND_DISTANCE = builder.addConfig(ModConfigTag.defaultGunSilenceSoundDistance_path, 16, 0, Integer.MAX_VALUE);

        builder.addComment(ModConfigTag.defaultGunOtherSoundDistance_comment);
        DEFAULT_GUN_OTHER_SOUND_DISTANCE = builder.addConfig(ModConfigTag.defaultGunOtherSoundDistance_path, 16, 0, Integer.MAX_VALUE);

        builder.addComment(ModConfigTag.creativePlayerConsumeAmmo_comment);
        CREATIVE_PLAYER_CONSUME_AMMO = builder.addConfig(ModConfigTag.creativePlayerConsumeAmmo_path, true);

        builder.addComment(ModConfigTag.autoReloadWhenRespawn_comment);
        AUTO_RELOAD_WHEN_RESPAWN = builder.addConfig(ModConfigTag.autoReloadWhenRespawn_path, false);

        builder.finishBuild();
    }
}