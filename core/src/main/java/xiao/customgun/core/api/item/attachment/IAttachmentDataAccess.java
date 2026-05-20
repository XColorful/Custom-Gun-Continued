/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.resource.ResourceTag;

public interface IAttachmentDataAccess extends IAttachmentNBTAccess {

    /**
     * 获取配件ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getAttachmentLocation(ItemStack attachmentItem);
    void setAttachmentLocation(ItemStack attachmentItem, ResourceLocation location);

    /**
     * 获取配件类型，如无则返回 {@link AttachmentCategory#NONE}
     */
    @NotNull AttachmentCategory getAttachmentCategory(ItemStack attachmentItem);

    int getScopeViewIndex(ItemStack attachmentItem);
}
