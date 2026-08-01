/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;

import java.util.HashMap;
import java.util.Map;

public enum ScriptMethodType implements ResourceTag.ConstantTag, IScriptMethodType {
    // IGunModifier
    UPDATE_MODIFIER_CACHE(ScriptMethodTag.UPDATE_MODIFIER_CACHE, ScriptMethodTag.UPDATE_MODIFIER_CACHE_OLD1),
    // IGunRuntime
    START_BOLT(ScriptMethodTag.START_BOLT, null),
    TICK_BOLT(ScriptMethodTag.TICK_BOLT, null),
    SHOOTER_FIRE(ScriptMethodTag.SHOOTER_FIRE, null),
    GUN_FIRE(ScriptMethodTag.GUN_FIRE, ScriptMethodTag.GUN_FIRE_OLD1),
    CAN_RELOAD(ScriptMethodTag.CAN_RELOAD, null),
    START_RELOAD(ScriptMethodTag.START_RELOAD, null),
    TICK_RELOAD(ScriptMethodTag.TICK_RELOAD, null),
    INTERRUPT_RELOAD(ScriptMethodTag.INTERRUPT_RELOAD, null),
    TICK_HEAT(ScriptMethodTag.TICK_HEAT, null),
    CALCULATE_SPREAD(ScriptMethodTag.CALCULATE_SPREAD, ScriptMethodTag.CALCULATE_SPREAD_OLD1),
    HANDLE_SHOOT_HEAT(ScriptMethodTag.HANDLE_SHOOT_HEAT, null);

    public final String typeName;
    public final String typeNameOld;
    ScriptMethodType(String name, String nameOld) {
        this.typeName = name;
        this.typeNameOld = nameOld;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getConstantName() {
        return this.typeName;
    }

    @Override
    public @NotNull ScriptMethodType getScriptMethodType() {
        return this;
    }

    private static final Map<String, ScriptMethodType> METHOD_TYPES = new HashMap<>();

    static {
        for (ScriptMethodType method : ScriptMethodType.values()) {
            METHOD_TYPES.put(method.typeName, method);
            if (method.typeNameOld != null) METHOD_TYPES.put(method.typeNameOld, method);
        }
    }

    public static @Nullable ScriptMethodType fromString(String name) {
        return name != null ? METHOD_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
