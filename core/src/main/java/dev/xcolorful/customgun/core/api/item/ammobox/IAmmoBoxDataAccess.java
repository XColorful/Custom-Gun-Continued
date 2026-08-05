/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.ammobox;

import dev.xcolorful.customgun.core.api.item.ammo.IAmmoDataAccess;
import net.minecraft.world.item.ItemStack;

public interface IAmmoBoxDataAccess extends IAmmoDataAccess {

    int getBoxLevel(ItemStack ammoItem);
    void setBoxLevel(ItemStack ammoItem, int level);

    /**
     * 获取子弹盒状态掩码
     */
    int getStatusMask(ItemStack ammoItem);
    /**
     * 该状态不依赖写入的值
     */
    @Deprecated default void setStatusMask(ItemStack ammoItem, int statusMask) {}
}
