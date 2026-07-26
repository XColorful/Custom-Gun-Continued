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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.entity.IGunProjectile;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;

import java.util.function.Supplier;

@ApiStatus.Internal
public class _DefaultGunAttack {

    protected static void shoot(ShooterProperty shooterProperty,
                                @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                Supplier<Float> pitch, Supplier<Float> yaw) {
        // TODO
    }

    protected static void doBulletSpread(ShooterProperty shooterProperty,
                                         @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                         ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                         @NotNull IGunProjectile iGunProjectile, @NotNull Projectile projectile,
                                         int bulletId,
                                         float xRot, float yRot, float pow, float uncertainty) {
        float yOffset = 0;
        projectile.shootFromRotation(livingShooter, xRot, yRot, yOffset, pow, uncertainty);
    }

    protected static void melee(ShooterProperty shooterProperty,
                                @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        // TODO
    }
}
