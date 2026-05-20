/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IAttachment;

public interface IAttachmentGetter {

    static @Nullable IAttachment fromItemStack(@Nullable ItemStack attachmentItem) {
        if (attachmentItem == null) return null;
        return attachmentItem.getItem() instanceof IAttachment iAttachment ? iAttachment : null;
    }
}
