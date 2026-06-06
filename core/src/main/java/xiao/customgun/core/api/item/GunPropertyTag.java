/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item;

public class GunPropertyTag {

    // --------IGunDataAccess--------
    public static final String GUN_LOCATION = "gun_rl";
    public static final String GUN_DISPLAY_LOCATION = "gun_display_rl";

    // --------IGunStateAccess--------
    public static final String FIRE_MODE_TYPE = "fire_mode";
    public static final String HEAT = "heat";
    public static final String OVERHEAT_LOCK = "overheat_lock";
    public static final String ATTACHMENT_LOCK = "attachment_lock";
    public static final String LASER_COLOR = "laser_color";
    public static final String TOOLTIP_MASK = "tooltip_mask";

    // --------IGunAmmoDataAccess--------
    public static final String DUMMY_AMMO = "dummy_ammo";
    public static final String DUMMY_AMMO_LIMIT = "dummy_ammo_limit";
    public static final String MAG_AMMO = "mag_ammo";
    public static final String BARREL_AMMO = "barrel_ammo";

    // --------IGunAttachmentDataAccess--------
    public static final String ATTACHMENT_PREFIX = "attachment_";

    // --------IGunExpAccess--------
    public static final String GUN_EXP = "gun_exp";

    private GunPropertyTag() {}
}
