/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.block;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IBlock;

public interface IBlockGetter {

    static @Nullable IBlock fromItemStack(@Nullable ItemStack blockItem) {
        if (blockItem == null) return null;
        return blockItem.getItem() instanceof IBlock iBlock ? iBlock : null;
    }

    // --------Deprecated--------

    @Deprecated static @Nullable IBlock getIBlockOrNull(@Nullable ItemStack blockItem) {
        return fromItemStack(blockItem);
    }
}
