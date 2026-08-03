/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.item.ammo;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.assets.info.GunpackInfo;
import xiao.customgun.core.api.item.IAmmo;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.index.AmmoIndex;
import xiao.customgun.core.resource.instance.data.AmmoIndexInstance;

import java.util.function.Consumer;

public class _AmmoItem {

    public static @Nullable Component getName(IAmmo _this,
                                    @NotNull ItemStack ammoItem) {
        var ammoLocation = _this.getAmmoLocation(ammoItem);
        @Nullable AmmoIndexInstance ammoIndexInstance = ResourceApi.getAmmoIndexInstance(ammoLocation);
        if (ammoIndexInstance == null) return null;

        AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
        return ammoIndex.getNameLang();
    }

    public static void appendHoverText(IAmmo _this,
                                       ItemStack ammoItem, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        var ammoLocation = _this.getAmmoLocation(ammoItem);
        @Nullable AmmoIndexInstance ammoIndexInstance = ResourceApi.getAmmoIndexInstance(ammoLocation);
        if (ammoIndexInstance != null) {
            AmmoIndex ammoIndex = ammoIndexInstance.getPojo();
            @NotNull MutableComponent tooltipLang = ammoIndex.getTooltipLang()
                    .copy().withStyle(ChatFormatting.GRAY);
            builder.accept(tooltipLang);
        }

        GunpackInfo gunpackInfo = ClientResourceApi.getGunpackInfo(ammoLocation);
        if (gunpackInfo != null) {
            @NotNull MutableComponent nameLang = gunpackInfo.getNameLang()
                    .copy().withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.ITALIC);
            builder.accept(nameLang);
        }
    }
}
