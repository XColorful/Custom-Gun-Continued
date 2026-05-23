/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.ammobox;

import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.item.AmmoBoxProperty;
import xiao.customgun.core.api.item.ammo.AmmoDataAccessor;
import xiao.customgun.core.util.NBTUtils;

public interface AmmoBoxDataAccessor extends AmmoDataAccessor, IAmmoBoxDataAccess {

    // --------IAmmoDataAccess--------

    @Override
    default int getAmmoCount(ItemStack ammoItem) {
        return Math.max(0, NBTUtils.getInt(ammoItem, AmmoBoxProperty.AMMO_COUNT.getTagName()));
    }
    @Override
    default void setAmmoCount(ItemStack ammoItem, int ammoCount) {
        NBTUtils.setInt(ammoItem, AmmoBoxProperty.AMMO_COUNT.getTagName(), ammoCount);
    }

    // --------IAmmoBoxDataAccess--------
}
