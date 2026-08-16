/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.item.ammo;

import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.item.IAmmo;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.index.AmmoIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AmmoIndexInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class _AmmoItem {

    public static @Nullable Component getName(IAmmo _this,
                                    @NotNull ItemStack ammoItem) {
        var ammoLocation = _this.getAmmoLocation(ammoItem);
        @Nullable AmmoIndexInstance ammoIndexInstance = ResourceApi.getAmmoIndexInstance(ammoLocation);
        if (ammoIndexInstance == null) return null;

        AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
        return Component.translatable(ammoIndex.getNameLang());
    }

    public static void appendHoverText(IAmmo _this,
                                       ItemStack ammoItem, @Nullable Level level, List<Component> components, TooltipFlag isAdvanced) {
        var ammoLocation = _this.getAmmoLocation(ammoItem);
        @Nullable AmmoIndexInstance ammoIndexInstance = ResourceApi.getAmmoIndexInstance(ammoLocation);
        if (ammoIndexInstance != null) {
            AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
            @NotNull MutableComponent tooltipLang = Component.translatable(ammoIndex.getTooltipLang())
                    .withStyle(ChatFormatting.GRAY);
            components.add(tooltipLang);
        }

        GunpackInfo gunpackInfo = ClientResourceApi.getGunpackInfo(ammoLocation);
        if (gunpackInfo != null) {
            @NotNull MutableComponent nameLang = Component.translatable(gunpackInfo.getNameLang())
                    .withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
            components.add(nameLang);
        }
    }
}
