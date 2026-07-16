/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.script;

import com.google.common.collect.Maps;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import xiao.customgun.client.api.animation.ObjectAnimation;
import xiao.customgun.core.api.script.LuaLibrary;

import java.util.Map;

public class LuaAnimationLib implements LuaLibrary {

    private final Map<String, Object> constantMap = Maps.newHashMap();

    public LuaAnimationLib() {

        // 映射 PlayType 枚举
        for (var playType : ObjectAnimation.PlayType.values()) {
            constantMap.put(playType.name(), playType.ordinal());
        }
    }

    @Override
    public void install(LuaValue chunk) {
        for(Map.Entry<String, Object> entry : constantMap.entrySet()) {
            chunk.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
        }
    }
}
