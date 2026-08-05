/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.builder;

import dev.xcolorful.customgun.core.api.item.AmmoProperty;
import dev.xcolorful.customgun.core.api.item.IAmmo;
import dev.xcolorful.customgun.core.api.item.ammo.IAmmoGetter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class AmmoBuilder extends ItemBuilder<AmmoBuilder> {

    private final IAmmo iAmmo;

    private AmmoBuilder(IAmmo iAmmo, ItemStack ammoItem) {
        super(ammoItem);
        this.iAmmo = iAmmo;
    }
    public static AmmoBuilder create(ItemLike ammo) {
        ItemStack ammoItem = new ItemStack(ammo);
        IAmmo iAmmo = IAmmoGetter.fromItemStack(ammoItem);
        if (iAmmo != null) return new AmmoBuilder(iAmmo, ammoItem);
        else throw new IllegalArgumentException("Item is not a IAmmo");
    }

    public <T> AmmoBuilder setProperty(AmmoProperty property, Class<T> type, T value) {
        property.set(this.iAmmo, this.itemStack, value);
        return this;
    }
}
