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
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.gun.state.IGunActionManager;

public class GunActionManager implements IGunActionManager {
    public static final GunActionManager INSTANCE = new GunActionManager();

    protected GunActionManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunActionManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IGunStateRuntime--------

    @Override
    public boolean startBolt(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        return false;
    }
    @Override
    public boolean tickBolt(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        return false;
    }

    @Override
    public boolean canReload(ItemStack gunItem, LivingEntity livingShooter) {
        return false;
    }
    @Override
    public boolean startReload(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        return false;
    }
    @Override
    public ReloadState tickReload(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {
        return null;
    }
    @Override
    public void interruptReload(ShooterProperty shooterProperty, ItemStack gunItem, LivingEntity livingShooter) {

    }
}
