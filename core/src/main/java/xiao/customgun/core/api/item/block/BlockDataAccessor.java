/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.BlockProperty;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.data.BlockData;
import xiao.customgun.core.resource.data.index.BlockIndex;
import xiao.customgun.core.util.NBTUtils;

public interface BlockDataAccessor extends IBlockDataAccess {

    // --------IBlockDataAccess--------

    @Override
    default @NotNull ResourceLocation getBlockLocation(ItemStack blockItem) {
        var blockLocation = NBTUtils.getResourceLocation(blockItem, BlockProperty.BLOCK_LOCATION.getTagName());
        return blockLocation != null ? blockLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setBlockLocation(ItemStack blockItem, ResourceLocation blockLocation) {
        NBTUtils.setResourceLocation(blockItem, BlockProperty.BLOCK_LOCATION.getTagName(), blockLocation);
    }

    // --------IBlockPojoGetter--------

    @Override
    default @Nullable BlockIndex getBlockIndex(ItemStack blockItem) {
        var indexLocation = this.getBlockLocation(blockItem);
        return ResourceApi.getBlockIndex(indexLocation);
    }
    @Override
    default @Nullable BlockData getBlockData(ItemStack blockItem) {
        @Nullable BlockIndex blockIndex = this.getBlockIndex(blockItem);
        if (blockIndex == null) return null;
        return ResourceApi.getBlockData(blockIndex.getDataLocation());
    }
}
