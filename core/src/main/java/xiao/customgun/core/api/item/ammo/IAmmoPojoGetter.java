/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.ammo;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.data.index.AmmoIndex;

public interface IAmmoPojoGetter {

    @Nullable AmmoIndex getAmmoIndex(ItemStack ammoItem);
}
