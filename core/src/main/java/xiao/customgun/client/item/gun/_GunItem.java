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
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.core.api.item.IGun;

public class _GunItem {

    public static @Nullable Component getName(IGun iGun, @NotNull ItemStack gunItem) {
        var gunLocation = iGun.getGunLocation(gunItem);
        ClientGunIndexInstance clientGunIndexInstance = ClientResourceApi.getClientGunIndexInstance(gunLocation);
        if (clientGunIndexInstance == null) return null;

        return clientGunIndexInstance.getPojo().getNameLang();
    }
}
