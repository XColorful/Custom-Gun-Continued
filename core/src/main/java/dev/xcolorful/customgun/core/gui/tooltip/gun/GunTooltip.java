/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gui.tooltip.gun;

import dev.xcolorful.customgun.core.api.item.AmmoProperty;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.builder.AmmoBuilder;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record GunTooltip(ItemStack gunItem, IGun iGun,
                         // --------Cache--------
                         ItemStack ammoItem,
                         ResourceLocation gunLocation)
        implements TooltipComponent {

    public static @Nullable GunTooltip fromItem(@Nullable ItemStack gunItem) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return null;

        var gunLocation = iGun.getGunLocation(gunItem);

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return null;

        GunData gunData = gunIndexInstance.getGunData();
        var ammoLocation = gunData.getAmmoLocation();
        ItemStack ammoItem = AmmoBuilder.create(ModItems.AMMO.get())
                .setProperty(AmmoProperty.AMMO_LOCATION,
                        ResourceLocation.class,
                        ammoLocation)
                .build();
        return new GunTooltip(gunItem, iGun,
                ammoItem,
                gunLocation);
    }
}
