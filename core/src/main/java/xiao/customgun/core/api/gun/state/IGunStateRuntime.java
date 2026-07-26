/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.state;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;

public interface IGunStateRuntime {

    /**
     * 过热 tick 处理
     * 默认不做任何事情
     */
    void tickHeat(ShooterProperty shooterProperty,
                  @NotNull IGun iGun, @NotNull ItemStack gunItem,
                  ILivingShooter iLivingShooter, LivingEntity livingShooter);
}
