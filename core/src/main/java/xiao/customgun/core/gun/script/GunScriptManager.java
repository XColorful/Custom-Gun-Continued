/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.script;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.gun.script.GunScriptApi;
import xiao.customgun.core.api.gun.script.IGunScriptManager;
import xiao.customgun.core.api.item.gun.modifier.GunModifierType;
import xiao.customgun.core.api.script.ScriptMethodType;

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
            LuaTable script = scriptApi.getScript();
            if (script == null) return value;

            LuaValue function = script.get(ScriptMethodType.UPDATE_MODIFIER_CACHE.getConstantName());
            if (!function.isfunction()) { // 检查是否是函数
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
