/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gui.tooltip.attachment;

import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record AttachmentTooltip(ItemStack attachmentItem, IAttachment iAttachment,
                                // --------Cache--------
                                ResourceLocation attachmentLocation,
                                AttachmentCategory attachmentCategory)
        implements TooltipComponent {

    public static @Nullable AttachmentTooltip fromItem(@Nullable ItemStack attachmentItem) {
        @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if (iAttachment == null) return null;

        var attachmentLocation = iAttachment.getAttachmentLocation(attachmentItem);
        AttachmentCategory attachmentCategory = iAttachment.getAttachmentCategory(attachmentItem);
        return new AttachmentTooltip(attachmentItem, iAttachment,
                attachmentLocation,
                attachmentCategory);
    }
}
