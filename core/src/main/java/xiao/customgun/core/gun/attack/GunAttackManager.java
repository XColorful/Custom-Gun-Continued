/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.attack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.event.shooter.ShooterPrepareMeleeEvent;
import xiao.customgun.core.api.gun.attack.IGunAttackManager;
import xiao.customgun.core.api.gun.script.GunScriptApi;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.MeleeType;
import xiao.customgun.core.api.script.ScriptMethodType;

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
    public void shoot(ShooterProperty shooterProperty,
                      @NotNull IGun iGun, @NotNull ItemStack gunItem,
                      ILivingShooter iLivingShooter, LivingEntity livingShooter,
                      Supplier<Float> pitch, Supplier<Float> yaw) { // TODO 这两个参数写到GunScriptApi还是lua函数参数?
        GunScriptApi scriptApi = GunScriptApi.of(iLivingShooter, livingShooter, iGun, gunItem);
        switch (scriptApi.simpleCall(ScriptMethodType.SHOOT)) {
            case TRUE, FALSE -> {}
            case UNKNOWN -> _DefaultGunAttack.shoot(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, pitch, yaw);
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
                    Vector2d spreadOffset = new Vector2d(
                            luaTable.get(1).checkdouble(),
                            luaTable.get(2).checkdouble()
                    );
                    iGunProjectile.shootFromRotation(livingShooter, xRot, yRot, 0, pow, spreadOffset);
                    return;
                }
            } catch (Exception e) {
                CustomGun.LOGGER.error("GunAttackManager: Failed to do bullet spread", e);
            }
        }

        _DefaultGunAttack.doBulletSpread(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, iGunProjectile, projectile, bulletId, xRot, yRot, pow, uncertainty);
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
