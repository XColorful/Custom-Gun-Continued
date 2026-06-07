/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.client.item.attachment._AttachmentItem;
import xiao.customgun.core.api.item.IAttachment;
import xiao.customgun.core.api.item.attachment.AttachmentDataAccessor;
import xiao.customgun.core.init.registry.ModItems;

public class AttachmentItem extends Item implements IAttachment, AttachmentDataAccessor {

    protected AttachmentItem(Properties properties) {
        super(properties);
    }
    public AttachmentItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY);
    }

    // --------Client--------

    @Override
    public @NotNull Component getName(@NotNull ItemStack attachmentItem) {
        var name = _AttachmentItem.getName(this, attachmentItem);
        return name != null ? name : super.getName(attachmentItem);
    }
}
