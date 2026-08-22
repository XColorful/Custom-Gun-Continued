/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gun.attack;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.IGunProjectile;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterFireEvent;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterPrepareMeleeEvent;
import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackManager;
import dev.xcolorful.customgun.core.api.gun.attack.IGunAttackRuntime;
import dev.xcolorful.customgun.core.api.script.context.GunScriptApi;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.MeleeType;
import dev.xcolorful.customgun.core.api.script.ScriptMethodType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.function.Supplier;

public class GunAttackManager implements IGunAttackManager {
    public static final GunAttackManager INSTANCE = new GunAttackManager();

    protected GunAttackManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunAttackManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IGunAttackRuntime--------

    @Override
    public @NotNull IGunAttackRuntime.ShooterFireResult shooterFire(ShooterProperty shooterProperty,
                                                                    @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                                    ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                                                    Supplier<Float> pitch, Supplier<Float> yaw,
                                                                    float clientChargeProgress) {
        McLogicalSide logicalSide = CustomGun.getSideExecutor().getLogicalSide();
        if (CustomGun.getEventPoster().postCustomEvent(new ShooterFireEvent(logicalSide,
                iLivingShooter, livingShooter, iGun, gunItem))) {
            return ShooterFireResult.ERROR;
        }

        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        return switch (scriptApi.simpleCall(ScriptMethodType.SHOOTER_FIRE)) {
            case TRUE -> ShooterFireResult.SUCCESS;
            case FALSE -> ShooterFireResult.ERROR;
            case UNKNOWN -> _DefaultGunAttack.shooterFire(logicalSide, shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, pitch, yaw, clientChargeProgress);
        };
    }
    @Override
    public @NotNull IGunAttackRuntime.GunFireResult gunFire(ShooterProperty shooterProperty,
                                                            @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                            ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                                            Supplier<Float> pitch, Supplier<Float> yaw) {
        McLogicalSide logicalSide = CustomGun.getSideExecutor().getLogicalSide();

        // 客户端侧提前返回，以继续客户端逻辑
        if (logicalSide.isClient()) return GunFireResult.SUCCESS;

        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        return switch (scriptApi.simpleCall(ScriptMethodType.GUN_FIRE)) {
            case TRUE -> GunFireResult.SUCCESS;
            case FALSE -> GunFireResult.ERROR;
            case UNKNOWN -> _DefaultGunAttack.gunFire(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, pitch, yaw);
        };
    }

    @Override
    public void doBulletSpread(ShooterProperty shooterProperty,
                               @NotNull IGun iGun, @NotNull ItemStack gunItem,
                               ILivingShooter iLivingShooter, LivingEntity livingShooter,
                               @NotNull IGunProjectile iGunProjectile, @NotNull Projectile projectile,
                               int bulletId,
                               float xRot, float yRot, float pow, float uncertainty) {
        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        @Nullable LuaFunction function = scriptApi.getFunction(ScriptMethodType.CALCULATE_SPREAD);
        if (function != null) {
            try {
                LuaValue luaValue = function.call(
                        CoerceJavaToLua.coerce(scriptApi),
                        LuaValue.valueOf(bulletId),
                        LuaValue.valueOf(uncertainty)
                );
                if (luaValue != null && luaValue.istable()) {
                    LuaTable luaTable = luaValue.checktable();
                    Vec2 spreadOffset = new Vec2(
                            (float) luaTable.get(1).checkdouble(),
                            (float) luaTable.get(2).checkdouble()
                    );
                    iGunProjectile.shootFromRotation(livingShooter, projectile, xRot, yRot, 0, pow, spreadOffset);
                    return;
                }
            } catch (Exception e) {
                CustomGun.LOGGER.error("GunAttackManager: Failed to do bullet spread", e);
            }
        }

        _DefaultGunAttack.doBulletSpread(livingShooter, iGunProjectile, projectile, xRot, yRot, pow, uncertainty);
    }

    @Override
    public @Nullable MeleePreparation prepareMelee(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                   ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        McLogicalSide logicalSide = CustomGun.getSideExecutor().getLogicalSide();
        if (CustomGun.getEventPoster().postCustomEvent(new ShooterPrepareMeleeEvent(logicalSide,
                iLivingShooter, livingShooter, iGun, gunItem))) {
            return null;
        }

        return _DefaultGunAttack.prepareMelee(iGun, gunItem, iLivingShooter, livingShooter);
    }
    @Override
    public void melee(ShooterProperty shooterProperty,
                      @NotNull IGun iGun, @NotNull ItemStack gunItem,
                      ILivingShooter iLivingShooter, LivingEntity livingShooter,
                      MeleeType meleeType) {
        _DefaultGunAttack.melee(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, meleeType);
    }
}
