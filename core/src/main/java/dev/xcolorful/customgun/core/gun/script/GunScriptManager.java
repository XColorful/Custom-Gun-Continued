/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gun.script;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.gun.script.context.GunScriptApi;
import dev.xcolorful.customgun.core.api.gun.script.IGunScriptManager;
import dev.xcolorful.customgun.core.api.item.gun.modifier.GunModifierType;
import dev.xcolorful.customgun.core.api.script.ScriptMethodType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

public class GunScriptManager implements IGunScriptManager {
    public static final GunScriptManager INSTANCE = new GunScriptManager();

    protected GunScriptManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunScriptManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IGunScriptRuntime--------

    @Override
    public @NotNull <V> V evalByScript(ItemStack gunItem, GunScriptApi scriptApi, GunModifierType modifierType, @NotNull V value) {
        try {
            @Nullable LuaFunction function = scriptApi.getFunction(ScriptMethodType.UPDATE_MODIFIER_CACHE);
            if (function == null) { // 检查是否是函数
                CustomGun.LOGGER.warn("GunScriptManager: Missing function {}", ScriptMethodType.UPDATE_MODIFIER_CACHE.getConstantName());
                return value;
            }

            LuaValue result = function.call(
                    CoerceJavaToLua.coerce(scriptApi),
                    LuaValue.valueOf(modifierType.getConstantName()),
                    CoerceJavaToLua.coerce(value)
            );

            if (result.isnil()) {
                CustomGun.LOGGER.warn("GunScriptManager: Lua returned nil, modifier={}", modifierType.getConstantName());
                return value;
            }

            return (V) CoerceLuaToJava.coerce(result, value.getClass());
        } catch (Exception e) {
            CustomGun.LOGGER.warn("GunScriptManager: Failed to evaluate modifier {}", modifierType.getConstantName(), e);
            return value;
        }
    }
}
