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

    // IAmmoExpAccess
    public static final String AMMO_LEVEL = AmmoPropertyTag.AMMO_LEVEL;

    // IAmmoBoxDataAccess
    public static final String AMMO_COUNT = "ammo_count";

    private AmmoBoxPropertyTag() {}
}
