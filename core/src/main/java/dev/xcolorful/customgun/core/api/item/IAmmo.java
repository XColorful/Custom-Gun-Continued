/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item;

import dev.xcolorful.customgun.core.api.item.ammo.IAmmoDataAccess;
import dev.xcolorful.customgun.core.api.item.ammo.IAmmoGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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
