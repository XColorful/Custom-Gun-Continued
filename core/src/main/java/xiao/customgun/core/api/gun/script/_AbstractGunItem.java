/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.script;

import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.item.IGun;

@Deprecated
public record _AbstractGunItem(IGun iGun, ItemStack gunItem) {
}
