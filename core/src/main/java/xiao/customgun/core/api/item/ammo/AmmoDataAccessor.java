/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.ammo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.AmmoProperty;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.util.NBTUtils;

public interface AmmoDataAccessor extends IAmmoDataAccess {

    // --------IAmmoDataAccess--------

    @Override
    default @NotNull ResourceLocation getAmmoLocation(ItemStack ammoItem) {
        var ammoLocation = NBTUtils.getResourceLocation(ammoItem, AmmoProperty.AMMO_LOCATION.getTagName());
        return ammoLocation != null ? ammoLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setAmmoLocation(ItemStack ammoItem, ResourceLocation ammoLocation) {
        NBTUtils.setResourceLocation(ammoItem, AmmoProperty.AMMO_LOCATION.getTagName(), ammoLocation);
    }
}
