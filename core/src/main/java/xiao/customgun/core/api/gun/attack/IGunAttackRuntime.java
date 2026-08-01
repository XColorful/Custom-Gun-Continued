/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.attack;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.MeleeType;

import java.util.function.Supplier;

public interface IGunAttackRuntime {

    /**
     * 射手射击时触发
     */
    @NotNull ShooterFireResult shooterFire(ShooterProperty shooterProperty,
                                           @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                           ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                           Supplier<Float> pitch, Supplier<Float> yaw);
    enum ShooterFireResult {
        SUCCESS,
        ERROR;
        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }
    /**
     * 枪械射击时触发，在客户端执行时仅触发事件
     */
    @NotNull GunFireResult gunFire(ShooterProperty shooterProperty,
                                   @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                   ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                   Supplier<Float> pitch, Supplier<Float> yaw);
    enum GunFireResult {
        SUCCESS,
        ERROR;
        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }

    /**
     * 初始化子弹角度和速度
     * @param shooterProperty 状态数据
     * @param gunItem         枪械物品
     * @param livingShooter   射击者
     * @param projectile      子弹
     * @param bulletId        多弹丸的子弹序数
     * @param xRot            射击方向
     * @param yRot            射击方向
     * @param pow             修正后的子弹初速
     * @param uncertainty     修正后的子弹不准确度
     */
    void doBulletSpread(ShooterProperty shooterProperty,
                        @NotNull IGun iGun, @NotNull ItemStack gunItem,
                        ILivingShooter iLivingShooter, LivingEntity livingShooter,
                        @NotNull IGunProjectile iGunProjectile, @NotNull Projectile projectile,
                        int bulletId,
                        float xRot, float yRot, float pow, float uncertainty);

    /**
     * 准备近战
     * @return 是否成功准备
     */
    @Nullable MeleePreparation prepareMelee(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                            ILivingShooter iLivingShooter, LivingEntity livingShooter);
    record MeleePreparation(int prepareTick,
                            @NotNull MeleeType meleeType) {}

    /**
     * 近战时调用
     */
    void melee(ShooterProperty shooterProperty,
               @NotNull IGun iGun, @NotNull ItemStack gunItem,
               ILivingShooter iLivingShooter, LivingEntity livingShooter,
               MeleeType meleeType);

    // --------Deprecated--------

    @Deprecated(forRemoval = true) default void shoot(ShooterProperty shooterProperty,
                                                      @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                      ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                                      Supplier<Float> pitch, Supplier<Float> yaw) {
        this.gunFire(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter, pitch, yaw);
    }
}
