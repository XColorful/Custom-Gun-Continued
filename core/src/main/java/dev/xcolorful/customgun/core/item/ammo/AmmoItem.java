/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.ammo;

import dev.xcolorful.customgun.client.item.ammo._AmmoItem;
import dev.xcolorful.customgun.core.api.item.IAmmo;
import dev.xcolorful.customgun.core.api.item.ammo.AmmoDataAccessor;
import dev.xcolorful.customgun.core.api.minecraft.item.ItemType;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class AmmoItem extends Item implements IAmmo, AmmoDataAccessor {

    protected AmmoItem(Properties properties) {
        super(properties);
    }
    public AmmoItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY.apply(ItemType.AMMO.getRegistryLocation()));
    }

    // --------Client--------

    @Override
    public @NotNull Component getName(@NotNull ItemStack ammoItem) {
        var name = _AmmoItem.getName(this, ammoItem);
        return name != null ? name : super.getName(ammoItem);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack ammoItem, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        _AmmoItem.appendHoverText(this, ammoItem, context, display, builder, tooltipFlag);
    }
}
