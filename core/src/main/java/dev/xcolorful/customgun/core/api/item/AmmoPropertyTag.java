/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item;

public class AmmoPropertyTag {

    // --------IAmmoDataAccess--------
    public static final String AMMO_LOCATION = "ammo_rl";
    public static final String AMMO_COUNT = "ammo_count";
    public static final String INFINITE_FEED = "infinite_feed";
    public static final String ALMIGHTY_AMMO = "almighty_ammo";

    // --------IAmmoStateAccess--------
    public static final String TOOLTIP_MASK = "tooltip_mask";

    // --------IAmmoExpAccess--------
    public static final String AMMO_LEVEL = "ammo_level";

    private AmmoPropertyTag() {}
}
