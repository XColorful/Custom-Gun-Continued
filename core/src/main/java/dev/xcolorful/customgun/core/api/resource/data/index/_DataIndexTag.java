/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.resource.data.index;

public abstract class _DataIndexTag {

    // 翻译键
    public static final String NAME_LANG = "name_lang"; public static final String NAME_LANG_OLD1 = "name";
    public static final String TOOLTIP_LANG = "tooltip_lang"; public static final String TOOLTIP_LANG_OLD1 = "tooltip";

    // data/displayIndex
    public static final String DATA_LOCATION = "data_location"; public static final String DATA_LOCATION_OLD1 = "data";
    public static final String DISPLAY_INDEX_LOCATION = "display_index_location"; public static final String DISPLAY_INDEX_LOCATION_OLD1 = "display";

    // 创造模式物品栏索引 (排序)
    public static final String SLOT_SORT = "slot_sort"; public static final String SLOT_SORT_OLD1 = "sort";

    protected _DataIndexTag() {}
}