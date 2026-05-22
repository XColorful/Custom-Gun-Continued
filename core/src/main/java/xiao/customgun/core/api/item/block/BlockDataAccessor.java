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
import xiao.customgun.core.api.item.BlockProperty;
import xiao.customgun.core.api.resource.ResourceTag;
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
}
