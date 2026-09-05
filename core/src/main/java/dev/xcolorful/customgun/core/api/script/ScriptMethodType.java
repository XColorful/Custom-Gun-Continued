/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.script;

import dev.xcolorful.customgun.core.api.gun.action.IGunActionRuntime;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.Map;

public enum ScriptMethodType implements ResourceTag.ConstantTag, IScriptMethodType {
    // IGunModifier
    UPDATE_MODIFIER_CACHE(ScriptMethodTag.UPDATE_MODIFIER_CACHE, ScriptMethodTag.UPDATE_MODIFIER_CACHE_OLD1),
    // IGunRuntime
    /**
     * 脚本实现该方法时，不应该引入副作用，即满足{@link IGunActionRuntime#startBolt}
     */
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
    HANDLE_SHOOT_HEAT(ScriptMethodTag.HANDLE_SHOOT_HEAT, null),
    // AnimStateMachine
    ANIM_INIT(ScriptMethodTag.ANIM_INIT, ScriptMethodTag.ANIM_INIT_OLD1),
    ANIM_EXIT(ScriptMethodTag.ANIM_EXIT, ScriptMethodTag.ANIM_EXIT_OLD1),
    ANIM_STATES(ScriptMethodTag.ANIM_STATES, ScriptMethodTag.ANIM_STATES_OLD1),
    // IAnimationStateContext
    ANIM_CONTEXT_UPDATE(ScriptMethodTag.ANIM_CONTEXT_UPDATE, ScriptMethodTag.ANIM_CONTEXT_UPDATE_OLD1),
    ANIM_CONTEXT_ENTRY_ACTION(ScriptMethodTag.ANIM_CONTEXT_ENTRY_ACTION, ScriptMethodTag.ANIM_CONTEXT_ENTRY_ACTION_OLD1),
    ANIM_CONTEXT_EXIT(ScriptMethodTag.ANIM_CONTEXT_EXIT, ScriptMethodTag.ANIM_CONTEXT_EXIT_OLD1),
    ANIM_CONTEXT_TRANSITION(ScriptMethodTag.ANIM_CONTEXT_TRANSITION, ScriptMethodTag.ANIM_CONTEXT_TRANSITION_OLD1),
    ;

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

    public @Nullable LuaFunction getFunction(LuaTable luaTable) {
        if (luaTable == null) return null;

        LuaValue function = luaTable.get(this.typeName);
        if (function.isfunction()) return function.checkfunction();
        else if (this.typeNameOld != null) {
            function = luaTable.get(this.typeNameOld);
            if (function.isfunction()) return function.checkfunction();
            else return null;
        } else return null;
    }
    public @Nullable LuaValue getFunctionOrNil(LuaTable luaTable) {
        if (luaTable == null) return null;

        LuaValue function = luaTable.get(this.typeName);
        if (function.isfunction()) return function.checkfunction();
        if (!function.isnil()) return null;

        if (this.typeNameOld != null) {
            function = luaTable.get(this.typeNameOld);
            if (function.isfunction()) return function.checkfunction();
            if (!function.isnil()) return null;
        }

        return LuaValue.NIL;
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
