/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.builder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import xiao.customgun.core.api.item.AttachmentProperty;
import xiao.customgun.core.api.item.IAttachment;
import xiao.customgun.core.api.item.attachment.IAttachmentGetter;

public class AttachmentBuilder {

    private final IAttachment iAttachment;
    private final ItemStack attachmentItem;

    private AttachmentBuilder(IAttachment iAttachment, ItemStack attachmentItem) {
        this.iAttachment = iAttachment;
        this.attachmentItem = attachmentItem;
    }
    public static AttachmentBuilder create(ItemLike attachment) {
        ItemStack attachmentItem = new ItemStack(attachment);
        IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
        if(iAttachment != null) return new AttachmentBuilder(iAttachment, attachmentItem);
        else throw new IllegalArgumentException("Item is not a attachment");
    }

    public <T> AttachmentBuilder setProperty(AttachmentProperty property, Class<T> type, T value) {
        property.set(this.iAttachment, this.attachmentItem, value);
        return this;
    }

    public ItemStack build() {
        return this.attachmentItem;
    }
}
