/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.sound.gun;

public class GunSoundTypeTag {
    // 射击相关
    public static final String SHOOT_SOUND = "shoot";
    public static final String SHOOT_3P_SOUND = "shoot_3p";
    public static final String SILENCE_SOUND = "silence";
    public static final String SILENCE_3P_SOUND = "silence_3p";

    // 近战相关
    public static final String MELEE_BAYONET = "melee_bayonet";
    public static final String MELEE_PUSH = "melee_push";
    public static final String MELEE_STOCK = "melee_stock";

    // 动作相关
    public static final String DRY_FIRE_SOUND = "dry_fire";
    public static final String RELOAD_EMPTY_SOUND = "reload_empty";
    public static final String RELOAD_TACTICAL_SOUND = "reload_tactical";
    public static final String INSPECT_EMPTY_SOUND = "inspect_empty";
    public static final String INSPECT_SOUND = "inspect";
    public static final String DRAW_SOUND = "draw";
    public static final String PUT_AWAY_SOUND = "put_away";
    public static final String BOLT_SOUND = "bolt";
    public static final String SWITCH_FIRE_MODE = "switch_fire_mode"; public static final String SWITCH_FIRE_MODE_OLD1 = "fire_select";

    // 反馈相关
    public static final String HEAD_HIT_SOUND = "head_hit";
    public static final String FLESH_HIT_SOUND = "flesh_hit";
    public static final String KILL_SOUND = "kill";

    private GunSoundTypeTag() {}
}