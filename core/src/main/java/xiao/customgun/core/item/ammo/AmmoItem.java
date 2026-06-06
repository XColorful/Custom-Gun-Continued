/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.ammo;

import net.minecraft.world.item.Item;
import xiao.customgun.core.init.registry.ModItems;

public class AmmoItem extends Item {

    protected AmmoItem(Properties properties) {
        super(properties);
    }
    public AmmoItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY);
    }
}
