/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.state;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.gun.state.IGunStateManager;

public class GunStateManager implements IGunStateManager {
    public static final GunStateManager INSTANCE = new GunStateManager();

    protected GunStateManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunStateManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IGunStateRuntime--------

    @Override
    public void tickHeat(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
    }
}
