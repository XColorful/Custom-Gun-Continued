/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.ammobox;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IAmmoBox;
import xiao.customgun.core.api.item.ammo.IAmmoGetter;

public interface IAmmoBoxGetter extends IAmmoGetter {

    static @Nullable IAmmoBox fromItemStack(@Nullable ItemStack ammoBoxItem) {
        if (ammoBoxItem == null) return null;
        return ammoBoxItem.getItem() instanceof IAmmoBox iAmmoBox ? iAmmoBox : null;
    }

    // --------Deprecated--------

    @Deprecated static @Nullable IAmmoBox getIAmmoBoxOrNull(@Nullable ItemStack ammoBoxItem) {
        return fromItemStack(ammoBoxItem);
    }
}
