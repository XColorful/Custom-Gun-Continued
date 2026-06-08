/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.builder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import xiao.customgun.core.api.item.AmmoProperty;
import xiao.customgun.core.api.item.IAmmo;
import xiao.customgun.core.api.item.ammo.IAmmoGetter;

public class AmmoBuilder {

    private final IAmmo iAmmo;
    private final ItemStack ammoItem;

    private AmmoBuilder(IAmmo iAmmo, ItemStack ammoItem) {
        this.iAmmo = iAmmo;
        this.ammoItem = ammoItem;
    }
    public static AmmoBuilder create(ItemLike ammo) {
        ItemStack ammoItem = new ItemStack(ammo);
        IAmmo iAmmo = IAmmoGetter.fromItemStack(ammoItem);
        if (iAmmo != null) return new AmmoBuilder(iAmmo, ammoItem);
        else throw new IllegalArgumentException("Item is not a IAmmo");
    }

    public <T> AmmoBuilder setProperty(AmmoProperty property, Class<T> type, T value) {
        property.set(this.iAmmo, this.ammoItem, value);
        return this;
    }

    public ItemStack build() {
        return this.ammoItem;
    }
}
