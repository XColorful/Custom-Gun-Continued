/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment;

import net.minecraft.world.item.Item;
import xiao.customgun.core.api.item.attachment.AttachmentDataAccessor;
import xiao.customgun.core.init.registry.ModItems;

public class AttachmentItem extends Item implements AttachmentDataAccessor {

    protected AttachmentItem(Properties properties) {
        super(properties);
    }
    public AttachmentItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY);
    }
}
