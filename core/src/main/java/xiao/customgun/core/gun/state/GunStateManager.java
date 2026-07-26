/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.state;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.gun.script.GunScriptApi;
import xiao.customgun.core.api.gun.state.IGunStateManager;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.script.ScriptMethodType;

public class GunStateManager implements IGunStateManager {
    public static final GunStateManager INSTANCE = new GunStateManager();

    protected GunStateManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunStateManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IGunStateRuntime--------

    @Override
    public void tickHeat(ShooterProperty shooterProperty,
                         @NotNull IGun iGun, @NotNull ItemStack gunItem,
                         ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        long heatTimestamp = shooterProperty != null ? shooterProperty.heatTimestamp : -1;
        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        @Nullable LuaFunction function = scriptApi.getFunction(ScriptMethodType.TICK_HEAT);
        if (function != null) {
            function.call(
                    CoerceJavaToLua.coerce(scriptApi),
                    LuaValue.valueOf(heatTimestamp)
            );
            return;
        }

        _DefaultGunState.tickHeat(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
    }
}
