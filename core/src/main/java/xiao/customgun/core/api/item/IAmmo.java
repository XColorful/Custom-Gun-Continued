/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.ammo.IAmmoDataAccess;
import xiao.customgun.core.api.item.ammo.IAmmoGetter;

public interface IAmmo extends IAmmoDataAccess, IAmmoGetter,
        IPojoItem {

    @Override
    default @NotNull Identifier getPojoLocation(ItemStack ammoItem) {
        return this.getAmmoLocation(ammoItem);
    }
    @Override
    default void setPojoLocation(ItemStack ammoItem, Identifier ammoLocation) {
        this.setAmmoLocation(ammoItem, ammoLocation);
    }
}
