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
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.entity.shooter.LivingShooterShoot;

import java.util.function.Supplier;

public interface IGunAttackRuntime {

    /**
     * 射击时触发
     * TODO 把{@link LivingShooterShoot}里枪械本身的判断移到IGunAttackRuntime里，把这个shoot改成boolean，避免像原版ScriptAPI里又检查一次
     */
    void shoot(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter,
               Supplier<Float> pitch, Supplier<Float> yaw);
    /**
     * 初始化子弹角度和速度
     * @param shooterProperty 状态数据
     * @param gunItem 枪械物品
     * @param livingShooter 射击者
     * @param projectile 子弹
     * @param bulletId 多弹丸的子弹序数
     * @param processedSpeed 修正后的子弹初速
     * @param inaccuracy 修正后的子弹不准确度
     * @param pitch 射击方向
     * @param yaw 射击方向
     */
    void doBulletSpread(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter,
                        Projectile projectile, int bulletId, float processedSpeed,
                        float inaccuracy, float pitch, float yaw);

    /**
     * 近战时调用
     */
    void melee(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter);
}
