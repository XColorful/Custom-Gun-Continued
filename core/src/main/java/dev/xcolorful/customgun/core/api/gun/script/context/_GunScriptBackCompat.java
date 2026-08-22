/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.gun.script.context;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;

import java.util.function.Supplier;

/**
 * 主要部分已移至{@link IGunScriptBackCompat}
 */
@Deprecated(forRemoval = false)
public class _GunScriptBackCompat {

    /**
     * 执行一次完整的射击逻辑，会考虑玩家的状态(是否在瞄准、是否在移动、是否在匍匐等)、配件数值影响、多弹丸散射、连发，播放开火音效、
     * @param consumeAmmo 本次射击是否消耗弹药
     */
    protected static void shootOnce(GunScriptApi _this, boolean consumeAmmo) {
        @Nullable ILivingShooter iLivingShooter = _this.iLivingShooter;
        @Nullable ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        _this.iGun.gunFire(shooterProperty,
                _this.iGun, _this.gunItem,
                iLivingShooter, _this.livingShooter,
                _this.pitchSupplier, _this.yawSupplier);
    }

    /**
     * 设置本次射击的额外伤害倍率（0-256）。此方法仅在射击流程期间可用，非射击调用时没有任何意义
     */
    protected static void setShotDamageMultiplier(GunScriptApi _this, float multiplier) {
        _this.shotDamageMultiplier =  Mth.clamp(multiplier, 0f, 256f);
    }

    /**
     * 获取本次射击的额外伤害倍率。此方法仅在射击流程期间可用，非射击调用时没有任何意义
     */
    protected static float getShotDamageMultiplier(GunScriptApi _this) {
        return _this.shotDamageMultiplier;
    }

    /**
     * 设置本次射击的额外弹速倍率（0-256）。此方法仅在射击流程期间可用，非射击调用时没有任何意义
     */
    protected static void setProjectileSpeedMultiplier(GunScriptApi _this, float multiplier) {
        _this.projectileSpeedMultiplier = Mth.clamp(multiplier, 0f, 256f);
    }

    /**
     * 获取本次射击的额外弹速倍率。此方法仅在射击流程期间可用，非射击调用时没有任何意义
     */
    protected static float getProjectileSpeedMultiplier(GunScriptApi _this) {
        return _this.projectileSpeedMultiplier;
    }

    @Deprecated
    static float clampMultiplier(float multiplier) {
        return Mth.clamp(multiplier, 0f, 256f);
    }

    /**
     * 获取在枪械 data 中声明的脚本参数
     * @return 脚本参数表
     */
    protected static LuaTable getScriptParam(GunScriptApi _this) {
        LuaTable param = _this.scriptParamsCache;
        return param == null ? new LuaTable() : param;
    }

    protected static _LuaNbtAccessor getNbt(GunScriptApi _this) {
        return _this.nbtUtil;
    }

    protected static _LuaEntityAccessor getEntityUtil(GunScriptApi _this) {
        if (_this.entityAccessor == null) _this.entityAccessor = _LuaEntityAccessor.of(_this.livingShooter);
        return _this.entityAccessor;
    }

    protected static void setShooter(GunScriptApi _this, LivingEntity shooter) {
        _this.livingShooter = shooter;
        _this.iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(_this.livingShooter);
    }

    protected static void setItemStack(GunScriptApi _this, ItemStack itemStack) {
        @Nullable IGun iGun = IGunGetter.fromItemStack(itemStack);
        if (iGun == null) {
            CustomGun.LOGGER.warn("_GunScriptBackCompat: itemStack {} is not IGun", itemStack.toString());
            return;
        }
        _this.gunItem = itemStack;
        _this.iGun = iGun;
        _this.resetCache();
    }

    protected static void setPitchSupplier(GunScriptApi _this, Supplier<Float> pitchSupplier) {
        _this.pitchSupplier = pitchSupplier;
    }

    protected static void setYawSupplier(GunScriptApi _this, Supplier<Float> yawSupplier) {
        _this.yawSupplier = yawSupplier;
    }
}
