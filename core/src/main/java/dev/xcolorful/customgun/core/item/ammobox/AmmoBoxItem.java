/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.ammobox;

import dev.xcolorful.customgun.client.item.ammobox._AmmoBoxItem;
import dev.xcolorful.customgun.core.api.item.IAmmoBox;
import dev.xcolorful.customgun.core.api.item.ammobox.AmmoBoxDataAccessor;
import dev.xcolorful.customgun.core.api.minecraft.item.ItemType;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AmmoBoxItem extends Item implements IAmmoBox, AmmoBoxDataAccessor {

    protected AmmoBoxItem(Properties properties) {
        super(properties);
    }
    public AmmoBoxItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY.apply(ItemType.AMMO_BOX.getRegistryLocation()));
    }

    // --------Client--------

    public static int getColor(ItemStack ammoItem, int tintIndex) {
        return _AmmoBoxItem.getColor(ammoItem, tintIndex);
    }

    public static float getStatus(ItemStack ammoItem, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        return _AmmoBoxItem.getStatus(ammoItem, level, entity, seed);
    }
}
