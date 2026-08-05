/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item;

import dev.xcolorful.customgun.core.api.item.ammobox.IAmmoBoxDataAccess;
import dev.xcolorful.customgun.core.api.item.ammobox.IAmmoBoxGetter;

public interface IAmmoBox extends IAmmo, IAmmoBoxDataAccess, IAmmoBoxGetter,
        IPojoItem {
}
