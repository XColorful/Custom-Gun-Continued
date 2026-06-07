/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.shoot;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.entity.ShooterProperty;

import java.util.function.Supplier;

public interface IGunAttackRuntime {

    /**
     * 射击时触发
     */
    void shoot(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter,
               Supplier<Float> pitch, Supplier<Float> yaw);

    /**
     * 近战时调用
     */
    void melee(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter);
}
