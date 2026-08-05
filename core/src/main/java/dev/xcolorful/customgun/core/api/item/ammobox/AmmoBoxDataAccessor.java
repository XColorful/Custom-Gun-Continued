/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.ammobox;

import dev.xcolorful.customgun.core.api.item.AmmoBoxProperty;
import dev.xcolorful.customgun.core.api.item.AmmoBoxPropertyTag;
import dev.xcolorful.customgun.core.api.item.ammo.AmmoDataAccessor;
import dev.xcolorful.customgun.core.util.NBTUtils;
import net.minecraft.world.item.ItemStack;

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

    @Override
    default int getBoxLevel(ItemStack ammoItem) {
        return Math.max(0, NBTUtils.getInt(ammoItem, AmmoBoxPropertyTag.BOX_LEVEL));
    }
    @Override
    default void setBoxLevel(ItemStack ammoItem, int boxLevel) {
        NBTUtils.setInt(ammoItem, AmmoBoxPropertyTag.BOX_LEVEL, boxLevel);
    }

    @Override
    default int getStatusMask(ItemStack ammoItem) {
        return AmmoBoxStatus.StatusMask.fromAmmoBox(ammoItem).toMask();
    }
}
