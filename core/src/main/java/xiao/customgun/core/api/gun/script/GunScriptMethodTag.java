/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.script;

import xiao.customgun.core.api.script.ScriptMethodTag;

@Deprecated(forRemoval = true)
public class GunScriptMethodTag {

    public static final String START_BOLT = ScriptMethodTag.START_BOLT;
    public static final String TICK_BOLT = ScriptMethodTag.TICK_BOLT;
    public static final String SHOOT = ScriptMethodTag.SHOOT;
    public static final String START_RELOAD = ScriptMethodTag.START_RELOAD;
    public static final String TICK_RELOAD = ScriptMethodTag.TICK_RELOAD;
    public static final String INTERRUPT_RELOAD = ScriptMethodTag.INTERRUPT_RELOAD;
    public static final String TICK_HEAT = ScriptMethodTag.TICK_HEAT;
    public static final String CALCULATE_SPREAD = ScriptMethodTag.CALCULATE_SPREAD;
    public static final String HANDLE_SHOOT_HEAT = ScriptMethodTag.HANDLE_SHOOT_HEAT;

    private GunScriptMethodTag() {}
}
