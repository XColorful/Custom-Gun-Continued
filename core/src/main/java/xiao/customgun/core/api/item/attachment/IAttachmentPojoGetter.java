/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.index.AttachmentIndex;

public interface IAttachmentPojoGetter {

    @Nullable AttachmentIndex getAttachmentIndex(ItemStack attachmentItem);
    @Nullable AttachmentData getAttachmentData(ItemStack attachmentItem);
}
