/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.minecraft.tab;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.item.GunProperty;
import xiao.customgun.core.api.item.builder.GunBuilder;
import xiao.customgun.core.api.item.gun.FireModeType;
import xiao.customgun.core.api.item.gun.GunCategory;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.init.registry.ModItems;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

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
            GunIndexInstance gunIndexInstance = entry.getValue();
            if (gunIndexInstance.getPojo().getGunCategory() == gunCategory) {
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
                        // 枪管子弹
                        .setProperty(GunProperty.BARREL_AMMO,
                                Integer.class,
                                1)
                        .build();
                gunItems.add(gunItem);
            }
        });
        return gunItems;
    }
}
