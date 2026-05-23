/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.block;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.data.data.BlockData;
import xiao.customgun.core.resource.data.index.BlockIndex;

public interface IBlockPojoGetter {

    @Nullable BlockIndex getBlockIndex(ItemStack blockItem);
    @Nullable BlockData getBlockData(ItemStack blockItem);
}
