/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.attachment;

import dev.xcolorful.customgun.client.api.item.attachment.AttachmentTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;

/**
 * 什么是配件的基础信息？除了加成信息以外还有什么？这个类预计的实现还是模糊的
 * extended mag也算做加成信息(对枪械有影响)，放在{@link AttachmentEnchantmentInfoPart}
 * 除了纯外观以外几乎都是加成，配件本身存在的意义就是影响数值
 */
public final class AttachmentBaseInfoPart extends AbstractTooltipPart implements AttachmentTooltipPart {
    public static final AttachmentBaseInfoPart INSTANCE = new AttachmentBaseInfoPart();
    private AttachmentBaseInfoPart() {}

    @Override
    public void build(ClientAttachmentTooltip.Context context) {
    }

    @Override
    public int measureHeight(ClientAttachmentTooltip.Context context) {
        if (!context.visibleParts.contains(AttachmentTooltipMask.BASE_INFO)) return 0;

        return 0;
    }
}
