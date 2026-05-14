/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.index;

import xiao.customgun.core.api.item.gun.GunCategory;

public class GunIndexTag extends _DataIndexTag {

    /**
     * 枪械种类 {@link GunCategory}
     */
    public static final String GUN_CATEGORY = "type";

    /**
     * ItemStack类型
     */
    public static final String ITEM_TYPE = "item_type";

    private GunIndexTag() {}
}
