/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gun.inventory;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.gun.inventory.IGunInventoryManager;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GunInventoryManager implements IGunInventoryManager {
    public static final GunInventoryManager INSTANCE = new GunInventoryManager();

    protected GunInventoryManager() {
    }
    public static final String _MANAGER_NAME = String.format("%s:%s", CustomGun.MOD_ID, GunInventoryManager.class.getSimpleName());
    @Override public String getManagerName() {
        return _MANAGER_NAME;
    }

    // --------IGunInventoryRuntime--------

    @Override
    public void retrieveAmmoFromGun(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                    @Nullable ILivingShooter iLivingShooter, @Nullable LivingEntity livingShooter) {
        _DefaultGunInventory.retrieveAmmoFromGun(iGun, gunItem, iLivingShooter, livingShooter);
    }

    @Override
    public int findAndExtractInventoryAmmo(@NotNull IInventoryCapability inventoryCapability,
                                           @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                           int requiredAmmoCount) {
        return _DefaultGunInventory.findAndExtractInventoryAmmo(inventoryCapability, iGun, gunItem, requiredAmmoCount);
    }

    @Override
    public int findAndExtractDummyAmmo(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                       int requiredAmmoCount) {
        return _DefaultGunInventory.findAndExtractDummyAmmo(iGun, gunItem, requiredAmmoCount);
    }
}
