/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.builder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import xiao.customgun.core.api.item.GunProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;

public final class GunBuilder extends ItemBuilder<GunBuilder> {

    private final IGun iGun;

    private GunBuilder(IGun iGun, ItemStack gunItem) {
        super(gunItem);
        this.iGun = iGun;
    }
    public static GunBuilder create(ItemLike gun) {
        ItemStack gunItem = new ItemStack(gun);
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun != null) return new GunBuilder(iGun, gunItem);
        else throw new IllegalArgumentException("Item is not a IGun");
    }

    public <T> GunBuilder setProperty(GunProperty property, Class<T> type, T value) {
        property.set(this.iGun, this.itemStack, value);
        return this;
    }
}
