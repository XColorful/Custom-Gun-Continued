/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gun.action;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ReloadState;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterReloadEvent;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterSwitchFireModeEvent;
import dev.xcolorful.customgun.core.api.gun.action.IGunActionManager;
import dev.xcolorful.customgun.core.api.script.context.GunScriptApi;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.script.ScriptMethodType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class GunActionManager implements IGunActionManager {
    public static final GunActionManager INSTANCE = new GunActionManager();

    protected GunActionManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunActionManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IGunActionRuntime--------

    @Override
    public boolean startBolt(ShooterProperty shooterProperty,
                             @NotNull IGun iGun, @NotNull ItemStack gunItem,
                             ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        return switch (scriptApi.simpleCall(ScriptMethodType.START_BOLT)) {
            case TRUE -> true;
            case FALSE -> false;
            case UNKNOWN -> _DefaultGunAction.startBolt(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
        };
    }
    @Override
    public boolean tickBolt(ShooterProperty shooterProperty,
                            @NotNull IGun iGun, @NotNull ItemStack gunItem,
                            ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        return switch (scriptApi.simpleCall(ScriptMethodType.TICK_BOLT)) {
            case TRUE -> true;
            case FALSE -> false;
            case UNKNOWN -> _DefaultGunAction.tickBolt(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
        };
    }

    @Override
    public boolean canReload(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                             ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        return switch (scriptApi.simpleCall(ScriptMethodType.CAN_RELOAD)) {
            case TRUE -> true;
            case FALSE -> false;
            case UNKNOWN -> _DefaultGunAction.canReload(iGun, gunItem, iLivingShooter, livingShooter);
        };
    }
    @Override
    public boolean startReload(ShooterProperty shooterProperty,
                               @NotNull IGun iGun, @NotNull ItemStack gunItem,
                               ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        // 换弹检查
        if (!iGun.canReload(iGun, gunItem, iLivingShooter, livingShooter)) return false;

        // 装弹事件钩子提前，不让脚本覆盖换弹事件
        McLogicalSide logicalSide = CustomGun.getSideExecutor().getLogicalSide();
        if (CustomGun.getEventPoster().postCustomEvent(new ShooterReloadEvent(logicalSide,
                iLivingShooter, livingShooter, iGun, gunItem))) {
            return false;
        }

        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        return switch (scriptApi.simpleCall(ScriptMethodType.START_RELOAD)) {
            case TRUE -> true;
            case FALSE -> false;
            case UNKNOWN -> _DefaultGunAction.startReload(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
        };
    }
    private static final ReloadState.StateType[] STATE_TYPES = ReloadState.StateType.values();
    @Override
    public ReloadState tickReload(ShooterProperty shooterProperty,
                                  @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                  ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        @Nullable LuaFunction function = scriptApi.getFunction(ScriptMethodType.TICK_RELOAD);
        if (function != null) {
            ReloadState reloadState = new ReloadState();
            Varargs varargs = function.invoke(CoerceJavaToLua.coerce(scriptApi));
            try {
                int typeOrdinary = varargs.arg(1).checkint();
                long countDown = varargs.arg(2).checklong();
                reloadState.setStateType(STATE_TYPES[typeOrdinary]);
                reloadState.setCountDown(countDown);
                return reloadState;
            } catch (Exception e) {
                CustomGun.LOGGER.error("GunActionManager: Failed to tick reload", e);
            }
        }

        return _DefaultGunAction.tickReload(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
    }
    @Override
    public void interruptReload(ShooterProperty shooterProperty,
                                @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        switch (scriptApi.simpleCall(ScriptMethodType.TICK_BOLT)) {
            case TRUE, FALSE -> {}
            case UNKNOWN -> _DefaultGunAction.interruptReload(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
        };
    }

    @Override
    public boolean switchFireMode(ShooterProperty shooterProperty,
                                  @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                  ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        McLogicalSide logicalSide = CustomGun.getSideExecutor().getLogicalSide();
        if (CustomGun.getEventPoster().postCustomEvent(new ShooterSwitchFireModeEvent(logicalSide,
                iLivingShooter, livingShooter, iGun, gunItem))) {
            return false;
        }

        return _DefaultGunAction.switchFireMode(shooterProperty, iGun, gunItem);
    }
}
