/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.gun;

import xiao.customgun.core.api.item.gun.GunDataAccessor;
import xiao.customgun.core.init.registry.ModItems;

public class GunItem extends AbstractGunItem implements GunDataAccessor {

    protected GunItem(Properties properties) {
        super(properties);
    }
    public GunItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY);
    }
}
