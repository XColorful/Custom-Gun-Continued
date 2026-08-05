/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.block;

import dev.xcolorful.customgun.core.api.item.BlockProperty;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.util.NBTUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface BlockDataAccessor extends IBlockDataAccess {

    // --------IBlockDataAccess--------

    @Override
    default @NotNull Identifier getBlockLocation(ItemStack blockItem) {
        var blockLocation = NBTUtils.getResourceLocation(blockItem, BlockProperty.BLOCK_LOCATION.getTagName());
        return blockLocation != null ? blockLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setBlockLocation(ItemStack blockItem, Identifier blockLocation) {
        NBTUtils.setResourceLocation(blockItem, BlockProperty.BLOCK_LOCATION.getTagName(), blockLocation);
    }
}
