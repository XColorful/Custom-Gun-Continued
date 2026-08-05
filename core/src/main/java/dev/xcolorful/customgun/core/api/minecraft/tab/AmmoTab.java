/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.minecraft.tab;

import dev.xcolorful.customgun.core.api.item.AmmoProperty;
import dev.xcolorful.customgun.core.api.item.ammo.AmmoCategory;
import dev.xcolorful.customgun.core.api.item.builder.AmmoBuilder;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

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
            @NotNull AmmoIndexInstance ammoIndexInstance = entry.getValue();
            {
                ItemStack ammoItem = AmmoBuilder.create(ModItems.AMMO.get())
                        // 子弹ResourceLocation
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
