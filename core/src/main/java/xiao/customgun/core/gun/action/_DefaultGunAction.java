/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.action;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;

@ApiStatus.Internal
public class _DefaultGunAction {

    protected static boolean tickBolt(ShooterProperty shooterProperty,
                                      @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                      ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        // TODO
        return true;
    }

    protected static boolean startReload(ShooterProperty shooterProperty,
                                         @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                         ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        // TODO
        return true;
    }

    protected static ReloadState tickReload(ShooterProperty shooterProperty,
                                            @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                            ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        // TODO
        return new ReloadState();
    }

    protected static void interruptReload(ShooterProperty shooterProperty,
                                          @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                          ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        // TODO
    }
}
