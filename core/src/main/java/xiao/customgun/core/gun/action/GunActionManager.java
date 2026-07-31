/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.action;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Varargs;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.event.shooter.ShooterReloadEvent;
import xiao.customgun.core.api.event.shooter.ShooterSwitchFireModeEvent;
import xiao.customgun.core.api.gun.action.IGunActionManager;
import xiao.customgun.core.api.gun.script.GunScriptApi;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.script.ScriptMethodType;
import xiao.customgun.core.event.EventPoster;

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
        if (EventPoster.get().postCustomEvent(new ShooterReloadEvent(logicalSide,
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
            Varargs varargs = function.invoke(function);
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
        if (EventPoster.get().postCustomEvent(new ShooterSwitchFireModeEvent(logicalSide,
                iLivingShooter, livingShooter, iGun, gunItem))) {
            return false;
        }

        return _DefaultGunAction.switchFireMode(shooterProperty, iGun, gunItem);
    }
}
