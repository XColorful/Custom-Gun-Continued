/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.resource.data.index;

import xiao.customgun.core.api.item.attachment.AttachmentCategory;

public class AttachmentIndexTag extends _DataIndexTag {

    /**
     * 配件种类 {@link AttachmentCategory}
     */
    public static final String ATTACHMENT_CATEGORY = "type";

    /**
     * 从创造模式物品栏和 JEI 中隐藏
     */
    public static final String HIDE_IN_GAME = "hidden";

    private AttachmentIndexTag() {}
}