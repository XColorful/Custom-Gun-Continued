/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.neoforge.item.gun;

import dev.xcolorful.customgun.core.item.gun.GunItem;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * 由于已经不需要注入BEWLR，为了{@link IClientItemExtensions}而使用的注册黑魔法已经不需要了
 * <br>
 * 但还是保留这个实现，以便浏览演变历史，以及以后可能需要重载平台接口
 */
public class NeoGunItem extends GunItem {
}
