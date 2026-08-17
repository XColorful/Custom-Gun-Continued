/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.attachment;

import dev.xcolorful.customgun.client.api.item.attachment.AttachmentTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;

public final class AttachmentEnchantmentInfoPart extends AbstractTooltipPart implements AttachmentTooltipPart {
    public static final AttachmentEnchantmentInfoPart INSTANCE = new AttachmentEnchantmentInfoPart();
    private AttachmentEnchantmentInfoPart() {}

    @Override
    public void build(ClientAttachmentTooltip.Context context) {
    }

    @Override
    public int measureHeight(ClientAttachmentTooltip.Context context) {
        if (!context.visibleParts.contains(AttachmentTooltipMask.ENCHANTMENT_INFO)) return 0;

        return 0;
    }
}
