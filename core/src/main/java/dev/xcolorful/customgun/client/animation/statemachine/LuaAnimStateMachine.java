/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.statemachine;

import dev.xcolorful.customgun.client.animation.controller.AnimController;
import dev.xcolorful.customgun.client.api.animation.statemachine.AnimStateContext;
import dev.xcolorful.customgun.client.api.animation.statemachine.AnimStateMachine;
import dev.xcolorful.customgun.client.api.animation.statemachine.IAnimationStateContext;
import dev.xcolorful.customgun.core.api.script.ScriptMethodType;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LuaAnimStateMachine<T extends AnimStateContext> extends AnimStateMachine<T> {

    Consumer<T> initializeFunc;
    Consumer<T> exitFunc;

    /**
     * 此方法不应该被直接调用，而是应该通过工厂生成实例
     *
     * @param animationController 动画状态机控制的动画控制器
     * @see Builder
     */
    private LuaAnimStateMachine(AnimController animationController) {
        super(animationController);
    }

    @Override
    public void initialize() {
        this.initializeFunc.accept(this.context);
        super.initialize();
    }

    @Override
    public void exit() {
        this.exitFunc.accept(this.context);
        super.exit();
    }

    public static class Builder<T extends AnimStateContext> {

        private AnimController controller;
        private LuaFunction initializeFunc;
        private LuaFunction exitFunc;
        private LuaFunction statesFunc;
        private LuaTable table;

        public Builder() {
        }

        public LuaAnimStateMachine<T> build() {
            if (controller == null) {
                throw new IllegalStateException("controller must not be null before build");
            }

            var stateMachine = new LuaAnimStateMachine<T>(controller);
            stateMachine.initializeFunc = (context) -> {
                if (initializeFunc != null) {
                    initializeFunc.call(table, CoerceJavaToLua.coerce(context));
                }
            };
            stateMachine.exitFunc = (context) -> {
                if (exitFunc != null) {
                    exitFunc.call(table, CoerceJavaToLua.coerce(context));
                }
            };
            stateMachine.setStatesSupplier(getStatesSupplier());
            return stateMachine;
        }

        public Builder<T> setController(AnimController controller) {
            this.controller = controller;
            return this;
        }

        public Builder<T> setLuaScripts(LuaTable table) {
            if (table == null) {
                return this;
            }
            this.table = table;
            this.initializeFunc = checkFunction(table, ScriptMethodType.ANIM_INIT);
            this.exitFunc = checkFunction(table, ScriptMethodType.ANIM_EXIT);
            this.statesFunc = checkFunction(table, ScriptMethodType.ANIM_STATES);
            return this;
        }

        private @Nullable LuaFunction checkFunction(LuaTable table, ScriptMethodType scriptMethodType) {
            @Nullable LuaValue function = scriptMethodType.getFunctionOrNil(table);
            if (function == null) {
                throw new LuaError("LuaAnimStateMachine.Builder: Either the type of field '" + scriptMethodType.getConstantName() + "' or '" + scriptMethodType.typeNameOld + "' must be function or nil");
            }

            if (function.isnil()) return null;

            return function.checkfunction();
        }

        @Deprecated(forRemoval = true)
        private void checkNullPointer() {
            if (controller == null) {
                throw new IllegalStateException("controller must not be null before build");
            }
        }

        private Supplier<Iterable<? extends IAnimationStateContext<T>>> getStatesSupplier() {
            if (statesFunc == null) {
                return null;
            }
            return () -> {
                LuaTable statesTable = statesFunc.call(table).checktable();
                LinkedList<LuaAnimStateContext<T>> states = new LinkedList<>();
                for (int f = 1; f <= statesTable.length(); f++) {
                    LuaTable stateTable = statesTable.get(f).checktable();
                    states.add(new LuaAnimStateContext<>(stateTable, table));
                }
                return states;
            };
        }
    }
}
