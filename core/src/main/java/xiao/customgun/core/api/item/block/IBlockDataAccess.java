/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.block;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.resource.ResourceTag;

public interface IBlockDataAccess extends IBlockPojoGetter {

    /**
     * 获取配件ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getBlockLocation(ItemStack blockItem);
    void setBlockLocation(ItemStack blockItem, Identifier blockLocation);
}
