/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.ammobox;

import dev.xcolorful.customgun.core.api.item.IAmmoBox;
import dev.xcolorful.customgun.core.api.item.ammo.IAmmoGetter;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
