/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.block;

import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IBlockDataAccess {

    /**
     * 获取配件ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getBlockLocation(ItemStack blockItem);
    void setBlockLocation(ItemStack blockItem, ResourceLocation blockLocation);
}
