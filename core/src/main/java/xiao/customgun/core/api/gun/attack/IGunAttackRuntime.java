/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.attack;

import net.minecraft.resources.Identifier;
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
                                           Supplier<Float> pitch, Supplier<Float> yaw,
                                           float clientChargeProgress);
    enum ShooterFireResult {
        SUCCESS(0),
        ERROR(0),
        // ----以下按判定优先级排序----
        OVERHEATED(1),
        NO_AMMO(2),
        NO_BARREL_AMMO(3), // 暂时不去扣枪膛(chamber)的细节，要改得整个模组范围里改，代码里还是用更好懂的说法
        NOT_CHARGED(4),
        ;

        public final int priority;
        ShooterFireResult(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

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
        SUCCESS(0),
        ERROR(0),
        // ----以下按判定优先级排序----
        OVERHEATED(1),
        AMMO_CONSUME_FAILED(2),
        // 暂时没检查充能 (仅用于burst + hold charging)
        ;
        public final int priority;
        GunFireResult(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }

        public boolean isSuccess() {
            return this == SUCCESS;
        }
    }
    // TODO 基本照搬移植的类，待重构
    class GunFirePropertyCache { // 当作record类，赋值后不再修改
        public Identifier gunLocation;
        public Identifier gunDisplayLocation;
        public Identifier ammoLocation;
        public float inaccuracy = 1f;
        public float soundDistance = 0;
        public boolean silenceSound = false;
        public float bulletSpeed = 0;
        public int bulletSplitAmount = 1;
        public int shootCount = 1;
        public long shootIntervalMs = 50;
        public GunFirePropertyCache() {
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
