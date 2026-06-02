/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.script;

import com.google.common.collect.Maps;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.item.gun.FireModeType;

import java.util.Map;

public class LuaGunLogicLib implements LuaLibrary {

    private final Map<String, Object> constantMap = Maps.newHashMap();

    public LuaGunLogicLib() {
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
