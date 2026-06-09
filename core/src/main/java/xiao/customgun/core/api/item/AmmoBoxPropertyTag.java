/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item;

public class AmmoBoxPropertyTag {

    // IAmmoDataAccess
    public static final String AMMO_LOCATION = AmmoPropertyTag.AMMO_LOCATION;
    public static final String INFINITE_FEED = AmmoPropertyTag.INFINITE_FEED;
    public static final String ALMIGHTY_AMMO = AmmoPropertyTag.ALMIGHTY_AMMO;

    // IAmmoStateAccess
    public static final String TOOLTIP_MASK = AmmoPropertyTag.TOOLTIP_MASK;

    // IAmmoExpAccess
    public static final String AMMO_LEVEL = AmmoPropertyTag.AMMO_LEVEL;

    // IAmmoBoxDataAccess
    public static final String AMMO_COUNT = "ammo_count";
    public static final String BOX_LEVEL = "box_level";
    public static final String STATUS_MASK = "status_mask";

    private AmmoBoxPropertyTag() {}
}
