/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource.assets;

import dev.xcolorful.customgun.CustomGun;

public class AssetsFolderName {

    public static final String GUNPACK_INFO = CustomGun.MOD_ID_SHORT + "_info"; @Deprecated public static final String GUNPACK_INFO_OLD1 = "";
    public static final String ANIMATIONS = CustomGun.MOD_ID_SHORT + "_animations"; public static final String ANIMATIONS_OLD1 = "animations";
    public static final String DISPLAY = CustomGun.MOD_ID_SHORT + "_display"; public static final String DISPLAY_OLD1 = "display";
    public static final String MODEL = CustomGun.MOD_ID_SHORT + "_models"; public static final String MODEL_OLD1 = "geo_models";
    public static final String LANG = "lang";
    public static final String PLAYER_ANIMATOR = "player_animator";
    public static final String SCRIPT = CustomGun.MOD_ID_SHORT + "_scripts"; public static final String SCRIPT_OLD1 = "scripts";
    public static final String SOUNDS = "sounds";
    public static final String MOD_SOUNDS = CustomGun.MOD_ID_SHORT + "_sounds"; public static final String MOD_SOUNDS_OLD1 = CustomGun.MOD_ID_OLD1 + "_sounds";
    public static final String TEXTURES = "textures";

    private AssetsFolderName() {}
}
