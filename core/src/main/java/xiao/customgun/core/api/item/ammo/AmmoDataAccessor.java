/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.ammo;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.AmmoProperty;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.index.AmmoIndex;
import xiao.customgun.core.util.NBTUtils;

public interface AmmoDataAccessor extends AmmoNBTAccessor, IAmmoDataAccess {

    // --------IAmmoDataAccess--------

    @Override
    default @NotNull Identifier getAmmoLocation(ItemStack ammoItem) {
        var customData = NBTUtils.getCustomData(ammoItem);
        if (customData == null) return ResourceTag.NULL_LOCATION;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getAmmoLocation(customDataTag);
    }
    @Override
    default void setAmmoLocation(ItemStack ammoItem, Identifier ammoLocation) {
        var customData = NBTUtils.getCustomData(ammoItem);
        if (customData == null) return;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setAmmoLocation(customDataTag, ammoLocation);
        NBTUtils.setCustomDataTag(ammoItem, customDataTag);
    }

    @Override
    default int getAmmoCount(ItemStack ammoItem) {
        return ammoItem.getCount();
    }
    @Override
    default void setAmmoCount(ItemStack ammoItem, int ammoCount) {
        ammoItem.setCount(ammoCount);
    }
    @Override
    default int consumeAmmo(ItemStack ammoItem, int amount) {
        int ammoCount = this.getAmmoCount(ammoItem);
        int consumed = Math.min(ammoCount, amount);
        // 消耗子弹
        if (!this.hasInfiniteFeed(ammoItem)) {
            this.setAmmoCount(ammoItem, ammoCount - consumed);
        }
        return consumed;
    }

    @Override
    default boolean hasInfiniteFeed(ItemStack ammoItem) {
        var customData = NBTUtils.getCustomData(ammoItem);
        if (customData == null) return false;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.hasInfiniteFeed(customDataTag);
    }
    @Override
    default void setInfiniteFeed(ItemStack ammoItem, boolean infiniteFeed) {
        var customData = NBTUtils.getCustomData(ammoItem);
        if (customData == null) return;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setInfiniteFeed(customDataTag, infiniteFeed);
        NBTUtils.setCustomDataTag(ammoItem, customDataTag);
    }

    @Override
    default boolean isAlmightyAmmo(ItemStack ammoItem) {
        var customData = NBTUtils.getCustomData(ammoItem);
        if (customData == null) return false;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.isAlmightyAmmo(customDataTag);
    }
    @Override
    default void setAlmightyAmmo(ItemStack ammoItem, boolean almighty) {
        var customData = NBTUtils.getCustomData(ammoItem);
        if (customData == null) return;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setAlmightyAmmo(customDataTag, almighty);
        NBTUtils.setCustomDataTag(ammoItem, customDataTag);
    }

    // --------IAmmoPojoGetter--------

    @Override
    default @Nullable AmmoIndex getAmmoIndex(ItemStack ammoItem) {
        var indexLocation = this.getAmmoLocation(ammoItem);
        return ResourceApi.getAmmoIndex(indexLocation);
    }

    // --------IAmmoExpAccess--------

    @Override
    default int getAmmoLevel(ItemStack ammoItem) {
        return Math.max(0, NBTUtils.getInt(ammoItem, AmmoProperty.AMMO_LEVEL.getTagName()));
    }
    @Override
    default void setAmmoLevel(ItemStack ammoItem, int ammoLevel) {
        NBTUtils.setInt(ammoItem, AmmoProperty.AMMO_LEVEL.getTagName(), ammoLevel);
    }
}
