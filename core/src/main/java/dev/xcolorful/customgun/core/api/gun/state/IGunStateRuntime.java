/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.gun.state;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.item.IGun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IGunStateRuntime {

    /**
     * 过热冷却 tick
     * <br>
     * 默认不做其他事情
     */
    void tickHeatCooldown(ShooterProperty shooterProperty,
                          @NotNull IGun iGun, @NotNull ItemStack gunItem,
                          ILivingShooter iLivingShooter, LivingEntity livingShooter);

    // --------Deprecated--------

    @Deprecated(forRemoval = false)
    default void tickHeat(ShooterProperty shooterProperty,
                          @NotNull IGun iGun, @NotNull ItemStack gunItem,
                          ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        this.tickHeatCooldown(shooterProperty, iGun, gunItem, iLivingShooter, livingShooter);
    }
}
