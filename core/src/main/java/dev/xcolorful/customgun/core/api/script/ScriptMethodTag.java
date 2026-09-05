/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.script;

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
    public static final String TICK_HEAT_COOLDOWN = "tick_heat_cooldown"; public static final String TICK_HEAT_COOLDOWN_OLD1 = "tick_heat";
    public static final String CALCULATE_SPREAD = "calculate_spread"; public static final String CALCULATE_SPREAD_OLD1 = "calcSpread";
    public static final String HANDLE_SHOOT_HEAT = "handle_shoot_heat";

    // AnimStateMachine
    public static final String ANIM_INIT = "anim_init"; public static final String ANIM_INIT_OLD1 = "initialize";
    public static final String ANIM_EXIT = "anim_exit"; public static final String ANIM_EXIT_OLD1 = "exit";
    public static final String ANIM_STATES = "anim_states"; public static final String ANIM_STATES_OLD1 = "states";

    // IAnimationStateContext
    public static final String ANIM_CONTEXT_UPDATE = "anim_context_update"; public static final String ANIM_CONTEXT_UPDATE_OLD1 = "update";
    public static final String ANIM_CONTEXT_ENTRY_ACTION = "anim_context_entry_action"; public static final String ANIM_CONTEXT_ENTRY_ACTION_OLD1 = "entry";
    public static final String ANIM_CONTEXT_EXIT = "anim_context_exit"; public static final String ANIM_CONTEXT_EXIT_OLD1 = "exit";
    public static final String ANIM_CONTEXT_TRANSITION = "anim_context_transition"; public static final String ANIM_CONTEXT_TRANSITION_OLD1 = "transition";

    private ScriptMethodTag() {}
}
