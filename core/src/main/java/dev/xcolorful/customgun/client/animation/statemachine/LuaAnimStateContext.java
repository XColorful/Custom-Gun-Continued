/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.statemachine;

import dev.xcolorful.customgun.client.api.animation.statemachine.AnimStateContext;
import dev.xcolorful.customgun.client.api.animation.statemachine.IAnimationStateContext;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import javax.annotation.Nullable;

public class LuaAnimStateContext<T extends AnimStateContext> implements IAnimationStateContext<T> {

    private final @NotNull LuaTable stateTable;
    private final @NotNull LuaTable scriptTable;
    private final @Nullable LuaFunction updateFunction;
    private final @Nullable LuaFunction enterFunction;
    private final @Nullable LuaFunction exitFunction;
    private final @Nullable LuaFunction transitionFunction;

    /**
     * 此方法用于通过 lua 脚本生成状态
     * <br>
     * 不应该被直接调用，而是通过工厂生成
     * @param stateTable 包含各个函数的表
     * @see LuaAnimStateMachine.Builder
     */
    LuaAnimStateContext(@NotNull LuaTable stateTable, @NotNull LuaTable scriptTable) {
        this.stateTable = stateTable;
        this.scriptTable = scriptTable;
        this.updateFunction = checkLuaFunction("update");
        this.enterFunction = checkLuaFunction("entry");
        this.exitFunction = checkLuaFunction("exit");
        this.transitionFunction = checkLuaFunction("transition");
    }

    @Override
    public void update(T context) {
        if (updateFunction != null) {
            updateFunction.call(scriptTable, CoerceJavaToLua.coerce(context));
        }
    }

    @Override
    public void entryAction(T context) {
        if (enterFunction != null) {
            enterFunction.call(scriptTable, CoerceJavaToLua.coerce(context));
        }
    }

    @Override
    public void exitAction(T context) {
        if (exitFunction != null) {
            exitFunction.call(scriptTable, CoerceJavaToLua.coerce(context));
        }
    }

    @Override
    public IAnimationStateContext<T> transition(T context, String condition) {
        if (transitionFunction != null) {
            LuaString conditionToLua = LuaString.valueOf(condition);
            LuaValue nextStateTable = transitionFunction.call(scriptTable, CoerceJavaToLua.coerce(context), conditionToLua);
            if (nextStateTable.istable()) {
                return new LuaAnimStateContext<>((LuaTable) nextStateTable, scriptTable);
            } else if (nextStateTable.isnil()) {
                return null;
            }
            throw new LuaError("the return of function 'transition' must be table or nil");
        }
        return null;
    }

    private LuaFunction checkLuaFunction(String funcName) {
        LuaValue value = stateTable.get(funcName);
        if (value.isfunction()) {
            return (LuaFunction) value;
        } else if (value.isnil()) {
            return null;
        }
        throw new LuaError("the type of field '" + funcName + "' must be function or nil");
    }
}
