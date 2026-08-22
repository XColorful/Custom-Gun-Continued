/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.config;

import dev.xcolorful.customgun.client.api.config.ClientModConfigTag;
import dev.xcolorful.customgun.client.api.textures.crosshair.CrosshairType;
import dev.xcolorful.customgun.core.api.config.IModConfigSpec;
import dev.xcolorful.customgun.core.api.config.IModConfigSpecBuilder;

public class RenderConfig {
    public static IModConfigSpec<Boolean> ENABLE_LASER_FADE_OUT;
    public static IModConfigSpec<Integer> GUN_LOD_RENDER_DISTANCE;
    public static IModConfigSpec<Boolean> DISABLE_GUN_TILTING;
    public static IModConfigSpec<Integer> BULLET_HOLE_PARTICLE_LIFE;
    public static IModConfigSpec<Double> BULLET_HOLE_PARTICLE_FADE_THRESHOLD;
    public static IModConfigSpec<CrosshairType> CROSSHAIR_TYPE;
    public static IModConfigSpec<Double> HIT_MARKET_START_POSITION;
    public static IModConfigSpec<Boolean> HEAD_SHOT_DEBUG_HITBOX;
    public static IModConfigSpec<Boolean> GUN_HUD_ENABLE;
    public static IModConfigSpec<Boolean> KILL_AMOUNT_ENABLE;
    public static IModConfigSpec<Double> KILL_AMOUNT_DURATION_SECOND;
    public static IModConfigSpec<Integer> TARGET_RENDER_DISTANCE;
    public static IModConfigSpec<Boolean> ENABLE_FIRST_PERSON_BULLET_TRACER;
    public static IModConfigSpec<Boolean> DISABLE_INTERACT_HUD_TEXT;
    public static IModConfigSpec<Boolean> AUTO_SELECT_GUN_SMITH_TABLE_FILTER;
    public static IModConfigSpec<Integer> DAMAGE_COUNTER_RESET_TIME;
    public static IModConfigSpec<Boolean> DISABLE_MOVEMENT_ATTRIBUTE_FOV;
    public static IModConfigSpec<Boolean> APPEND_RESOURCE_LOCATION_IN_TOOLTIP;
    public static IModConfigSpec<Boolean> BLOCK_ENTITY_TRANSLUCENT;

    public static void init(IModConfigSpecBuilder builder) {
        builder.startBuild(ClientModConfigTag.render_path);

        builder.addComment(ClientModConfigTag.enableLaserFadeOut_comment);
        ENABLE_LASER_FADE_OUT = builder.addConfig(ClientModConfigTag.enableLaserFadeOut_path, true);

        builder.addComment(ClientModConfigTag.gunLodRenderDistance_comment);
        GUN_LOD_RENDER_DISTANCE = builder.addConfig(ClientModConfigTag.gunLodRenderDistance_path, 0, 0, Integer.MAX_VALUE);

        builder.addComment(ClientModConfigTag.disableGunTilting_comment);
        DISABLE_GUN_TILTING = builder.addConfig(ClientModConfigTag.disableGunTilting_path, false);

        builder.addComment(ClientModConfigTag.bulletHoleParticleLife_comment);
        BULLET_HOLE_PARTICLE_LIFE = builder.addConfig(ClientModConfigTag.bulletHoleParticleLife_path, 400, 0, Integer.MAX_VALUE);

        builder.addComment(ClientModConfigTag.bulletHoleParticleFadeThreshold_comment);
        BULLET_HOLE_PARTICLE_FADE_THRESHOLD = builder.addConfig(ClientModConfigTag.bulletHoleParticleFadeThreshold_path, 0.98, 0.0, 1.0);

        builder.addComment(ClientModConfigTag.crosshairType_comment);
        CROSSHAIR_TYPE = builder.addConfig(ClientModConfigTag.crosshairType_path, CrosshairType.DOT_1);

        builder.addComment(ClientModConfigTag.hitMarkerStartPosition_comment);
        HIT_MARKET_START_POSITION = builder.addConfig(ClientModConfigTag.hitMarkerStartPosition_path, 4.0, -1024.0, 1024.0);

        builder.addComment(ClientModConfigTag.headShotDebugHitbox_comment);
        HEAD_SHOT_DEBUG_HITBOX = builder.addConfig(ClientModConfigTag.headShotDebugHitbox_path, false);

        builder.addComment(ClientModConfigTag.gunHUDEnable_comment);
        GUN_HUD_ENABLE = builder.addConfig(ClientModConfigTag.gunHUDEnable_path, true);

        builder.addComment(ClientModConfigTag.killAmountEnable_comment);
        KILL_AMOUNT_ENABLE = builder.addConfig(ClientModConfigTag.killAmountEnable_path, true);

        builder.addComment(ClientModConfigTag.killAmountDurationSecond_comment);
        KILL_AMOUNT_DURATION_SECOND = builder.addConfig(ClientModConfigTag.killAmountDurationSecond_path, 3.0, 0.0, Double.MAX_VALUE);

        builder.addComment(ClientModConfigTag.targetRenderDistance_comment);
        TARGET_RENDER_DISTANCE = builder.addConfig(ClientModConfigTag.targetRenderDistance_path, 128, 0, Integer.MAX_VALUE);

        builder.addComment(ClientModConfigTag.enableFirstPersonBulletTracer_comment);
        ENABLE_FIRST_PERSON_BULLET_TRACER = builder.addConfig(ClientModConfigTag.enableFirstPersonBulletTracer_path, true);
        FIRST_PERSON_BULLET_TRACER_ENABLE = ENABLE_FIRST_PERSON_BULLET_TRACER;

        builder.addComment(ClientModConfigTag.disableInteractHudText_comment);
        DISABLE_INTERACT_HUD_TEXT = builder.addConfig(ClientModConfigTag.disableInteractHudText_path, false);

        builder.addComment(ClientModConfigTag.autoSelectGunSmithTableFilter_comment);
        AUTO_SELECT_GUN_SMITH_TABLE_FILTER = builder.addConfig(ClientModConfigTag.autoSelectGunSmithTableFilter_path, true);

        builder.addComment(ClientModConfigTag.damageCounterResetTime_comment);
        DAMAGE_COUNTER_RESET_TIME = builder.addConfig(ClientModConfigTag.damageCounterResetTime_path, 2000, 10, Integer.MAX_VALUE);

        builder.addComment(ClientModConfigTag.disableMovementAttributeFov_comment);
        DISABLE_MOVEMENT_ATTRIBUTE_FOV = builder.addConfig(ClientModConfigTag.disableMovementAttributeFov_path, true);

        builder.addComment(ClientModConfigTag.appendResourceLocationInTooltip_comment);
        APPEND_RESOURCE_LOCATION_IN_TOOLTIP = builder.addConfig(ClientModConfigTag.appendResourceLocationInTooltip_path, true);
        ENABLE_RESOURCE_LOCATION_IN_TOOLTIP = APPEND_RESOURCE_LOCATION_IN_TOOLTIP;

        builder.addComment(ClientModConfigTag.blockEntityTranslucent_comment);
        BLOCK_ENTITY_TRANSLUCENT = builder.addConfig(ClientModConfigTag.blockEntityTranslucent_path, false);

        builder.finishBuild();
    }

    // --------Deprecated--------

    @Deprecated(forRemoval = true) public static IModConfigSpec<Boolean> FIRST_PERSON_BULLET_TRACER_ENABLE;
    @Deprecated(forRemoval = true) public static IModConfigSpec<Boolean> ENABLE_RESOURCE_LOCATION_IN_TOOLTIP;
}