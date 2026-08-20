/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gui.tooltip.ammo;

import dev.xcolorful.customgun.core.api.item.IAmmo;
import dev.xcolorful.customgun.core.api.item.ammo.IAmmoGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record AmmoTooltip(ItemStack ammoItem, IAmmo iAmmo,
                          // --------Cache--------
                          Identifier ammoLocation,
                          int ammoCount,
                          int ammoLevel)
        implements TooltipComponent {

    public static @Nullable AmmoTooltip fromItem(@Nullable ItemStack ammoItem) {
        @Nullable IAmmo iAmmo = IAmmoGetter.fromItemStack(ammoItem);
        if (iAmmo == null) return null;

        var ammoLocation = iAmmo.getAmmoLocation(ammoItem);
        int ammoCount = iAmmo.getAmmoCount(ammoItem);
        int ammoLevel = iAmmo.getAmmoLevel(ammoItem);
        return new AmmoTooltip(ammoItem, iAmmo,
                ammoLocation,
                ammoCount,
                ammoLevel);
    }
}
