/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.index.GunIndex;

public interface IGunPojoGetter {

    @Nullable GunIndex getGunIndex(ItemStack gunItem);
    @Nullable GunData getGunData(ItemStack gunItem);

    // --------便利接口--------
    default boolean getEnableCrawl(ItemStack gunItem) {
        @Nullable GunData gunData = this.getGunData(gunItem);
        if (gunData == null) return false;
        return gunData.getEnableCrawl();
    }
}
