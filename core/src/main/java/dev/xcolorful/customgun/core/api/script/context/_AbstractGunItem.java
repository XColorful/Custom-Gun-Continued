/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.script.context;

import dev.xcolorful.customgun.core.api.item.IGun;
import net.minecraft.world.item.ItemStack;

@Deprecated
public record _AbstractGunItem(IGun iGun, ItemStack gunItem) {
}
