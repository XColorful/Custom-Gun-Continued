/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.attachment;

import dev.xcolorful.customgun.client.api.item.attachment.AttachmentTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import net.minecraft.client.gui.Font;

/**
 * 配件状态
 * 倍镜状态是存在枪上的，所以不显示在配件里
 * 目前想不到纯配件物品有什么状态
 */
public final class AttachmentStateInfoPart extends AbstractTooltipPart implements AttachmentTooltipPart {
    public static final AttachmentStateInfoPart INSTANCE = new AttachmentStateInfoPart();
    private AttachmentStateInfoPart() {}

    @Override
    public void build(ClientAttachmentTooltip.Context context, Font font) {
    }

    @Override
    public int measureHeight(ClientAttachmentTooltip.Context context) {
        if (!context.visibleParts.contains(AttachmentTooltipMask.STATE_INFO)) return 0;

        return 0;
    }
}
