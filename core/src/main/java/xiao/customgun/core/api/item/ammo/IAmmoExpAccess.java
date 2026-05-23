/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.ammo;

import net.minecraft.world.item.ItemStack;

public interface IAmmoExpAccess {

    /**
     * 获取子弹等级
     */
    int getAmmoLevel(ItemStack ammoItem);
    void setAmmoLevel(ItemStack ammoItem, int ammoLevel);
}
