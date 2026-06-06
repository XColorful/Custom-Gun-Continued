/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.ammobox;

import net.minecraft.world.item.Item;
import xiao.customgun.core.init.registry.ModItems;

public class AmmoBoxItem extends Item {

    protected AmmoBoxItem(Properties properties) {
        super(properties);
    }
    public AmmoBoxItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY);
    }
}
