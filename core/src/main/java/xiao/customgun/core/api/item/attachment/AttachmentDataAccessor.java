/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.util.NBTUtils;

public interface AttachmentDataAccessor extends IAttachmentDataAccess {

    // --------IAttachmentDataAccess--------

    @Override
    default @NotNull ResourceLocation getAttachmentLocation(ItemStack attachmentItem) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return ResourceTag.NULL_LOCATION;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getAttachmentLocation(customDataTag);
    }
    @Override
    default void setAttachmentLocation(ItemStack attachmentItem, ResourceLocation location) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setAttachmentLocation(customDataTag, location);
        NBTUtils.setCustomDataTag(attachmentItem, customDataTag);
    }

    @Override
    default @NotNull AttachmentCategory getAttachmentCategory(ItemStack attachmentItem) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return AttachmentCategory.NONE;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getAttachmentCategory(customDataTag);
    }
}
