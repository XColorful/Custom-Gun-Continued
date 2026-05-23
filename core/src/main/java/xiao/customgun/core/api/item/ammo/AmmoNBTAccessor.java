/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.ammo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.AmmoProperty;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.util.NBTUtils;

public interface AmmoNBTAccessor extends IAmmoNBTAccess {

    AmmoNBTAccessor INSTANCE = new AmmoNBTAccessor() {};

    // --------IAmmoNBTAccess--------

    @Override
    default @NotNull Identifier getAmmoLocation(CompoundTag ammoItemCustomDataTag) {
        var ammoLocation = NBTUtils.getResourceLocation(ammoItemCustomDataTag, AmmoProperty.AMMO_LOCATION.getTagName());
        return ammoLocation != null ? ammoLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setAmmoLocation(CompoundTag ammoItemCustomDataTag, Identifier ammoLocation) {
        NBTUtils.setResourceLocation(ammoItemCustomDataTag, AmmoProperty.AMMO_LOCATION.getTagName(), ammoLocation);
    }

    @Override
    default boolean hasInfiniteFeed(CompoundTag ammoItemCustomDataTag) {
        return NBTUtils.getBoolean(ammoItemCustomDataTag, AmmoProperty.INFINITE_FEED.getTagName());
    }
    @Override
    default void setInfiniteFeed(CompoundTag ammoItemCustomDataTag, boolean infiniteFeed) {
        NBTUtils.setBoolean(ammoItemCustomDataTag, AmmoProperty.INFINITE_FEED.getTagName(), infiniteFeed);
    }

    @Override
    default boolean isAlmightyAmmo(CompoundTag ammoItemCustomDataTag) {
        return NBTUtils.getBoolean(ammoItemCustomDataTag, AmmoProperty.ALMIGHTY_AMMO.getTagName());
    }
    @Override
    default void setAlmightyAmmo(CompoundTag ammoItemCustomDataTag, boolean almighty) {
        NBTUtils.setBoolean(ammoItemCustomDataTag, AmmoProperty.ALMIGHTY_AMMO.getTagName(), almighty);
    }
}
