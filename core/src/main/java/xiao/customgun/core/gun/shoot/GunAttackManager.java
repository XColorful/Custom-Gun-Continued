/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.shoot;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.gun.shoot.IGunAttackManager;

import java.util.function.Supplier;

public class GunAttackManager implements IGunAttackManager {
    public static final GunAttackManager INSTANCE = new GunAttackManager();

    protected GunAttackManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunAttackManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IGunShootRuntime--------

    @Override
    public void shoot(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter, Supplier<Float> pitch, Supplier<Float> yaw) {
    }

    @Override
    public void melee(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
    }
}
