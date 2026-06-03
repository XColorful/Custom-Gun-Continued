/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data;

import xiao.customgun.CustomGun;

public class DataFolderName {

    public static final String GUNPACK_META = CustomGun.MOD_ID_SHORT + "_meta"; @Deprecated public static final String GUNPACK_META_OLD1 = "../../";
    public static final String DATA = CustomGun.MOD_ID_SHORT + "_data"; public static final String DATA_OLD1 = "data";
    public static final String INDEX = CustomGun.MOD_ID_SHORT + "_index"; public static final String INDEX_OLD1 = "index";
    public static final String RECIPE_FILTER = CustomGun.MOD_ID_SHORT + "_recipe_filters"; public static final String RECIPE_FILTER_OLD1 = "recipe_filter";
    public static final String RECIPE = "recipes"; // 原版目录名
    public static final String SCRIPT = CustomGun.MOD_ID_SHORT + "_scripts"; public static final String SCRIPT_OLD1 = "scripts";
    public static final String LOOT_INJECTOR = CustomGun.MOD_ID_OLD1 + "_loot_injectors";
    public static final String MOD_TAG = CustomGun.MOD_ID_SHORT + "_tags"; public static final String MOD_TAGS_OLD1 = CustomGun.MOD_ID_OLD1 + "_tags";

    private DataFolderName() {}
}
