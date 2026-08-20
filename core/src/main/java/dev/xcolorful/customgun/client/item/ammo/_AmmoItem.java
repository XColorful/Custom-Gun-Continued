/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.item.ammo;

import dev.xcolorful.customgun.core.api.item.IAmmo;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class _AmmoItem {

    public static @Nullable Component getName(IAmmo _this, @NotNull ItemStack ammoItem) {
        var ammoLocation = _this.getAmmoLocation(ammoItem);
        @Nullable AmmoIndexInstance ammoIndexInstance = ResourceApi.getAmmoIndexInstance(ammoLocation);
        if (ammoIndexInstance == null) return null;

        AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
        return Component.translatable(ammoIndex.getNameLang());
    }
}
