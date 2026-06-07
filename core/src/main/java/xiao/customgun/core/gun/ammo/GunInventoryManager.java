/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.ammo;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.gun.ammo.IGunInventoryManager;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;

public class GunInventoryManager implements IGunInventoryManager {
    public static final GunInventoryManager INSTANCE = new GunInventoryManager();

    protected GunInventoryManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunInventoryManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    @Override
    public void dropAllAmmo(ItemStack gunItem, LivingEntity livingShooter) {
    }

    @Override
    public int findAndExtractInventoryAmmo(IInventoryCapability inventoryCapability, ItemStack gunItem, int needAmmoCount) {
        return 0;
    }

    @Override
    public int findAndExtractDummyAmmo(ItemStack gunItem, int needAmmoCount) {
        return 0;
    }
}
