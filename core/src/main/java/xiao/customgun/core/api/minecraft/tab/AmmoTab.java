/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.minecraft.tab;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.item.AmmoProperty;
import xiao.customgun.core.api.item.ammo.AmmoCategory;
import xiao.customgun.core.api.item.builder.AmmoBuilder;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.init.registry.ModItems;
import xiao.customgun.core.resource.instance.data.AmmoIndexInstance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class AmmoTab {

    public static Comparator<Map.Entry<Identifier, AmmoIndexInstance>> indexSort() {
        return Comparator.comparingInt(entry -> entry.getValue().getPojo().getSlotSort());
    }

    public static List<ItemStack> buildAmmoItems(AmmoCategory ammoCategory) {
        List<ItemStack> ammoItems = new ArrayList<>();
        ResourceApi.getAllAmmoIndexInstance().stream().sorted(indexSort()).forEach(entry -> {
            AmmoIndexInstance ammoIndexInstance = entry.getValue();
            {
                ItemStack ammoItem = AmmoBuilder.create(ModItems.AMMO.get())
                        .setProperty(AmmoProperty.AMMO_LOCATION,
                                Identifier.class,
                                entry.getKey())
                        .build();
                ammoItems.add(ammoItem);
            }
        });
        return ammoItems;
    }
}
