/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.gun;

import net.minecraft.world.item.Item;
import xiao.customgun.core.api.item.IGun;

public abstract class AbstractGunItem extends Item implements IGun {

    protected AbstractGunItem(Properties properties) {
        super(properties);
    }
}
