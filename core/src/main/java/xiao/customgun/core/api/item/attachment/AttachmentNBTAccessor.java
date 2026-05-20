/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.AttachmentProperty;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.util.NBTUtils;

public interface AttachmentNBTAccessor extends IAttachmentNBTAccess {

    AttachmentNBTAccessor INSTANCE = new AttachmentNBTAccessor() {};

    // --------IAttachmentNBTAccess--------

    @Override
    default @NotNull ResourceLocation getAttachmentLocation(CompoundTag attachmentCustomDataTag) {
        var attachmentLocation = NBTUtils.getResourceLocation(attachmentCustomDataTag, AttachmentProperty.ATTACHMENT_LOCATION.getTagName());
        return attachmentLocation != null ? attachmentLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setAttachmentLocation(CompoundTag attachmentCustomDataTag, ResourceLocation attachmentLocation) {
        NBTUtils.setResourceLocation(attachmentCustomDataTag, AttachmentProperty.ATTACHMENT_LOCATION.getTagName(), attachmentLocation);
    }
    @Override
    default @NotNull AttachmentCategory getAttachmentCategory(CompoundTag attachmentCustomDataTag) {
        AttachmentCategory category = AttachmentCategory.fromString(NBTUtils.getString(attachmentCustomDataTag, AttachmentProperty.ATTACHMENT_CATEGORY.getTagName()));
        return category != null ? category : AttachmentCategory.NONE;
    }

    @Override
    default int getScopeViewIndex(CompoundTag attachmentCustomDataTag) {
        if (attachmentCustomDataTag == null) return 0;
        // TODO
        return 0;
    }
}
