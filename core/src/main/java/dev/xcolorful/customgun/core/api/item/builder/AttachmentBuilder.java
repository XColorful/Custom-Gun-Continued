/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.builder;

import dev.xcolorful.customgun.core.api.item.AttachmentProperty;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public final class AttachmentBuilder extends ItemBuilder<AttachmentBuilder> {

    private final IAttachment iAttachment;

    private AttachmentBuilder(IAttachment iAttachment, ItemStack attachmentItem) {
        super(attachmentItem);
        this.iAttachment = iAttachment;
    }
    public static AttachmentBuilder create(ItemLike attachment) {
        ItemStack attachmentItem = new ItemStack(attachment);
        @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if(iAttachment != null) return new AttachmentBuilder(iAttachment, attachmentItem);
        else throw new IllegalArgumentException("Item is not a IAttachment");
    }

    public <T> AttachmentBuilder setProperty(AttachmentProperty property, Class<T> type, T value) {
        property.set(this.iAttachment, this.itemStack, value);
        return this;
    }
}
