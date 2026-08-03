/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.item.gun;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.index.GunIndex;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

public class _GunItem {

    public static @Nullable Component getName(IGun iGun, @NotNull ItemStack gunItem) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return null;

        GunIndex gunIndex = gunIndexInstance.getPojo();
        return gunIndex.getNameLang();
    }
}
