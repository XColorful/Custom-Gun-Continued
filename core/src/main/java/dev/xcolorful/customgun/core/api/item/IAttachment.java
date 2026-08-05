/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item;

import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentDataAccess;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IAttachment extends IAttachmentDataAccess, IAttachmentGetter,
        IPojoItem {

    @Override
    default @NotNull Identifier getPojoLocation(ItemStack attachmentItem) {
        return this.getAttachmentLocation(attachmentItem);
    }
    @Override
    default void setPojoLocation(ItemStack attachmentItem, Identifier attachmentLocation) {
        this.setAttachmentLocation(attachmentItem, attachmentLocation);
    }
}
