/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gui.tooltip.gun;

import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.AmmoProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.builder.AmmoBuilder;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.init.registry.ModItems;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

public record GunTooltip(ItemStack gunItem, IGun iGun,
                         // --------Cache--------
                         ItemStack ammoItem,
                         Identifier gunLocation)
        implements TooltipComponent {

    public static @Nullable GunTooltip fromItem(@Nullable ItemStack gunItem) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return null;

        var gunLocation = iGun.getGunLocation(gunItem);

        GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return null;

        GunData gunData = gunIndexInstance.getGunData();
        var ammoLocation = gunData.getAmmoLocation();
        ItemStack ammoItem = AmmoBuilder.create(ModItems.AMMO.get())
                .setProperty(AmmoProperty.AMMO_LOCATION,
                        Identifier.class,
                        ammoLocation)
                .build();
        return new GunTooltip(gunItem, iGun,
                ammoItem,
                gunLocation);
    }
}
