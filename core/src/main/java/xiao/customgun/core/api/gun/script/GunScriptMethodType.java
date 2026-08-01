/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.script;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.api.script.IScriptMethodType;
import xiao.customgun.core.api.script.ScriptMethodType;

import java.util.HashMap;
import java.util.Map;

public enum GunScriptMethodType implements ResourceTag.ConstantTag, IScriptMethodType {
    START_BOLT(ScriptMethodType.START_BOLT),
    TICK_BOLT(ScriptMethodType.TICK_BOLT),
    SHOOTER_FIRE(ScriptMethodType.SHOOTER_FIRE),
    GUN_FIRE(ScriptMethodType.GUN_FIRE),
    CAN_RELOAD(ScriptMethodType.CAN_RELOAD),
    START_RELOAD(ScriptMethodType.START_RELOAD),
    TICK_RELOAD(ScriptMethodType.TICK_RELOAD),
    INTERRUPT_RELOAD(ScriptMethodType.INTERRUPT_RELOAD),
    TICK_HEAT(ScriptMethodType.TICK_HEAT),
    CALCULATE_SPREAD(ScriptMethodType.CALCULATE_SPREAD),
    HANDLE_SHOOT_HEAT(ScriptMethodType.HANDLE_SHOOT_HEAT);

    public final ScriptMethodType methodType;
    public final String typeName;
    public final String typeNameOld;
    GunScriptMethodType(ScriptMethodType methodType) {
        this.methodType = methodType;
        this.typeName = methodType.typeName;
        this.typeNameOld = methodType.typeNameOld;
    }
    @Override public String getTagName() {
        return this.typeName;
    }
    @Override public String getConstantName() {
        return this.typeName;
    }

    @Override
    public @NotNull ScriptMethodType getScriptMethodType() {
        return this.methodType;
    }

    private static final Map<String, GunScriptMethodType> METHOD_TYPES = new HashMap<>();

    static {
        for (GunScriptMethodType method : GunScriptMethodType.values()) {
            METHOD_TYPES.put(method.typeName, method);
            if (method.typeNameOld != null) METHOD_TYPES.put(method.typeNameOld, method);
        }
    }

    public static @Nullable GunScriptMethodType fromString(String name) {
        return name != null ? METHOD_TYPES.get(name) : null;
    }

    @Override
    public String toString() {
        return this.typeName;
    }
}
