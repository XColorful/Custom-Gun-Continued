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

import java.util.Collections;
import java.util.List;

public class AmmoConfig {
    public static IModConfigSpec<Boolean> EXPLOSIVE_AMMO_DESTROYS_BLOCK;
    public static IModConfigSpec<Boolean> EXPLOSIVE_AMMO_FIRE;
    public static IModConfigSpec<Boolean> EXPLOSIVE_AMMO_KNOCK_BACK;
    public static IModConfigSpec<Integer> EXPLOSIVE_AMMO_VISIBLE_DISTANCE;
    // TODO 这个得改ArraySet
    public static IModConfigSpec<List<String>> PASS_THROUGH_BLOCKS;
    public static IModConfigSpec<Boolean> DESTROY_GLASS;
    public static IModConfigSpec<Boolean> IGNITE_BLOCK;
    public static IModConfigSpec<Boolean> IGNITE_ENTITY;
    public static IModConfigSpec<Double> GLOBAL_BULLET_SPEED_MODIFIER;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ModConfigTag.ammo_path);

        builder.addComment(ModConfigTag.explosiveAmmoDestroysBlock_comment);
        EXPLOSIVE_AMMO_DESTROYS_BLOCK = builder.addConfig(ModConfigTag.explosiveAmmoDestroysBlock_path, true);

        builder.addComment(ModConfigTag.explosiveAmmoFire_comment);
        EXPLOSIVE_AMMO_FIRE = builder.addConfig(ModConfigTag.explosiveAmmoFire_path, false);

        builder.addComment(ModConfigTag.explosiveAmmoKnockBack_comment);
        EXPLOSIVE_AMMO_KNOCK_BACK = builder.addConfig(ModConfigTag.explosiveAmmoKnockBack_path, true);

        builder.addComment(ModConfigTag.explosiveAmmoVisibleDistance_comment);
        EXPLOSIVE_AMMO_VISIBLE_DISTANCE = builder.addConfig(ModConfigTag.explosiveAmmoVisibleDistance_path, 192, 0, Integer.MAX_VALUE);

        builder.addComment(ModConfigTag.passThroughBlocks_comment);
        PASS_THROUGH_BLOCKS = builder.addConfig(ModConfigTag.passThroughBlocks_path, Collections.emptyList());

        builder.addComment(ModConfigTag.destroyGlass_comment);
        DESTROY_GLASS = builder.addConfig(ModConfigTag.destroyGlass_path, true);

        builder.addComment(ModConfigTag.igniteBlock_comment);
        IGNITE_BLOCK = builder.addConfig(ModConfigTag.igniteBlock_path, true);

        builder.addComment(ModConfigTag.igniteEntity_comment);
        IGNITE_ENTITY = builder.addConfig(ModConfigTag.igniteEntity_path, true);

        builder.addComments(ModConfigTag.globalBulletSpeedModifier_comment);
        GLOBAL_BULLET_SPEED_MODIFIER = builder.addConfig(ModConfigTag.globalBulletSpeedModifier_path, 2.0, 0.01, 20.0);

        builder.finishBuild();
    }
}