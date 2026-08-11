/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.script;

import com.google.common.collect.Maps;
import dev.xcolorful.customgun.client.api.animation.statemachine.GunAnimationState;
import dev.xcolorful.customgun.core.api.entity.ReloadState;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.script.LuaLibrary;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Map;

/**
 * 在 Lua 脚本中引入 ContextConstant 定义的常量
 * <p>
 * 调用 install 方法将常量注入环境
 */
public class LuaGunAnimationLib implements LuaLibrary {

    private final Map<String, Object> constantMap = Maps.newHashMap();

    public LuaGunAnimationLib() {
        // 映射 GunAnimationState 常量字段
        for (GunAnimationState state : GunAnimationState.values()) {
            constantMap.put(state.name(), state.getConstantName());
        }

        // 映射 ReloadState.StateType 枚举
        for (ReloadState.StateType stateType : ReloadState.StateType.values()) {
            constantMap.put(stateType.name(), stateType.ordinal());
        }

        // 映射 FireMode 枚举
        for (var fireMode : FireModeType.values()) {
            constantMap.put(fireMode.name(), fireMode.ordinal());
        }
    }

    @Override
    public void install(LuaValue chunk) {
        for(Map.Entry<String, Object> entry : constantMap.entrySet()) {
            chunk.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
        }
    }
}
