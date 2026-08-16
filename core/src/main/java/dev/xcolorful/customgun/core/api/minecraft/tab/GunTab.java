/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.minecraft.tab;

import dev.xcolorful.customgun.core.api.item.GunProperty;
import dev.xcolorful.customgun.core.api.item.builder.GunBuilder;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.item.gun.GunCategory;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.index.GunIndex;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GunTab {

    public static Comparator<Map.Entry<ResourceLocation, GunIndexInstance>> indexSort() {
        return Comparator.comparingInt(entry -> entry.getValue().getPojo().getSlotSort());
    }

    public static List<ItemStack> buildGunItems(GunCategory gunCategory) {
        List<ItemStack> gunItems = new ArrayList<>();
        ResourceApi.getAllGunIndexInstance().stream().sorted(indexSort()).forEach(entry -> {
            @NotNull GunIndexInstance gunIndexInstance = entry.getValue();

            GunIndex gunIndex = gunIndexInstance.getPojo();
            if (gunIndex.getGunCategory() == gunCategory) {
                GunData gunData = gunIndexInstance.getGunData();
                ItemStack gunItem = GunBuilder.create(ModItems.GUN.get())
                        // 枪械ResourceLocation
                        .setProperty(GunProperty.GUN_LOCATION,
                                ResourceLocation.class,
                                entry.getKey())
                        // 开火模式
                        .setProperty(GunProperty.FIRE_MODE_TYPE,
                                FireModeType.class,
                                gunData.getFireModeTypes().get(0))
                        // 装满弹匣
                        .setProperty(GunProperty.MAG_AMMO,
                                Integer.class,
                                gunData.getDefaultMagSize())
                        .build();
                gunItems.add(gunItem);
            }
        });
        return gunItems;
    }
}
