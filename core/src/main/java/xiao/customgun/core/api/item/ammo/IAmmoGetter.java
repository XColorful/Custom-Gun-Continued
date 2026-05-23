/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.ammo;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IAmmo;

public interface IAmmoGetter {

    static @Nullable IAmmo fromItemStack(@Nullable ItemStack ammoItem) {
        if (ammoItem == null) return null;
        return ammoItem.getItem() instanceof IAmmo iAmmo ? iAmmo : null;
    }

    // --------Deprecated--------

    @Deprecated static @Nullable IAmmo getIAmmoOrNull(@Nullable ItemStack ammoItem) {
        return fromItemStack(ammoItem);
    }
}
