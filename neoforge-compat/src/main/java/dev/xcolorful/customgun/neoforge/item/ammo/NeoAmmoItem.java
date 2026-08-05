/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.neoforge.item.ammo;

import dev.xcolorful.customgun.core.item.ammo.AmmoItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class NeoAmmoItem extends AmmoItem {

    @OnlyIn(Dist.CLIENT)
    public static void registerClientExtension(Consumer<IClientItemExtensions> consumer) {
    }
}
