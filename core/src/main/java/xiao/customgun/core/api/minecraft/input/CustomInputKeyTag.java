/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.minecraft.input;

import xiao.customgun.CustomGun;

public class CustomInputKeyTag {

    public static final String PREFIX = "key." + CustomGun.MOD_ID + ".";

    // config
    @Deprecated(forRemoval = true) public static final String CONFIG = "config";
    // player
    public static final String INTERACT = "interact";
    public static final String REFIT = "refit";
    // shooter
    public static final String AIM = "aim";
    public static final String INSPECT = "inspect";
    public static final String MELEE = "melee";
    public static final String PRONE = "prone";
    public static final String RELOAD = "reload";
    public static final String SHOOT = "shoot";
    public static final String SWITCH_FIRE_MODE = "switch_fire_mode";
    public static final String ZOOM = "zoom";

    private CustomInputKeyTag() {}
}
