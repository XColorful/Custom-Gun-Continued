/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.script;

import com.google.common.collect.Maps;
import dev.xcolorful.customgun.client.api.animation.AnimationPlayType;
import dev.xcolorful.customgun.core.api.script.LuaLibrary;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Map;

public class LuaAnimationLib implements LuaLibrary {

    private final Map<String, Object> constantMap = Maps.newHashMap();

    public LuaAnimationLib() {

        // 映射 PlayType 枚举
        for (var playType : AnimationPlayType.values()) {
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
