/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.script;

public class ScriptMethodTag {

    // IGunModifier
    public static final String UPDATE_MODIFIER_CACHE = "update_modifier_cache"; public static final String UPDATE_MODIFIER_CACHE_OLD1 = "modify_cached_property";

    // IGunRuntime
    public static final String START_BOLT = "start_bolt";
    public static final String TICK_BOLT = "tick_bolt";
    public static final String SHOOTER_FIRE = "shooter_fire";
    public static final String GUN_FIRE = "gun_fire"; public static final String GUN_FIRE_OLD1 = "shoot";
    public static final String CAN_RELOAD = "can_reload";
    public static final String START_RELOAD = "start_reload";
    public static final String TICK_RELOAD = "tick_reload";
    public static final String INTERRUPT_RELOAD = "interrupt_reload";
    public static final String TICK_HEAT = "tick_heat";
    public static final String CALCULATE_SPREAD = "calculate_spread"; public static final String CALCULATE_SPREAD_OLD1 = "calSpread";
    public static final String HANDLE_SHOOT_HEAT = "handle_shoot_heat";

    private ScriptMethodTag() {}
}
