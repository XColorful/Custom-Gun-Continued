/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gui.tooltip.ammobox;

import dev.xcolorful.customgun.core.api.item.AmmoProperty;
import dev.xcolorful.customgun.core.api.item.IAmmoBox;
import dev.xcolorful.customgun.core.api.item.ammobox.IAmmoBoxGetter;
import dev.xcolorful.customgun.core.api.item.builder.AmmoBuilder;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record AmmoBoxTooltip(ItemStack ammoBoxItem, IAmmoBox iAmmoBox,
                             // --------Cache--------
                             ItemStack ammoItem,
                             Identifier ammoLocation,
                             int ammoCount)
        implements TooltipComponent {

    public static @Nullable AmmoBoxTooltip fromItem(@Nullable ItemStack ammoBoxItem) {
        @Nullable IAmmoBox iAmmoBox = IAmmoBoxGetter.fromItemStack(ammoBoxItem);
        if (iAmmoBox == null) return null;

        var ammoLocation = iAmmoBox.getAmmoLocation(ammoBoxItem);
        int ammoCount = iAmmoBox.getAmmoCount(ammoBoxItem);
        ItemStack ammoItem = AmmoBuilder.create(ModItems.AMMO.get())
                .setProperty(AmmoProperty.AMMO_LOCATION,
                        Identifier.class,
                        ammoLocation)
                .build();
        return new AmmoBoxTooltip(ammoBoxItem, iAmmoBox,
                ammoItem,
                ammoLocation,
                ammoCount);
    }
}
