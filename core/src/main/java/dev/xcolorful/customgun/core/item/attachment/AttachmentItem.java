/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.attachment;

import dev.xcolorful.customgun.client.item.attachment._AttachmentItem;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentDataAccessor;
import dev.xcolorful.customgun.core.api.minecraft.item.ItemType;
import dev.xcolorful.customgun.core.init.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AttachmentItem extends Item implements IAttachment, AttachmentDataAccessor {

    protected AttachmentItem(Properties properties) {
        super(properties);
    }
    public AttachmentItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY.apply(ItemType.ATTACHMENT.getRegistryLocation()));
    }

    // --------Client--------

    @Override
    public @NotNull Component getName(@NotNull ItemStack attachmentItem) {
        var name = _AttachmentItem.getName(this, attachmentItem);
        return name != null ? name : super.getName(attachmentItem);
    }
}
