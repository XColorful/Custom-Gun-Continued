/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.AttachmentProperty;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.util.NBTUtils;

public interface AttachmentNBTAccessor extends IAttachmentNBTAccess {

    AttachmentNBTAccessor INSTANCE = new AttachmentNBTAccessor() {};

    // --------IAttachmentNBTAccess--------

    @Override
    default @NotNull Identifier getAttachmentLocation(CompoundTag attachmentCustomDataTag) {
        var attachmentLocation = NBTUtils.getResourceLocation(attachmentCustomDataTag, AttachmentProperty.ATTACHMENT_LOCATION.getTagName());
        return attachmentLocation != null ? attachmentLocation : ResourceTag.NULL_LOCATION;
    }
    @Override
    default void setAttachmentLocation(CompoundTag attachmentCustomDataTag, Identifier attachmentLocation) {
        NBTUtils.setResourceLocation(attachmentCustomDataTag, AttachmentProperty.ATTACHMENT_LOCATION.getTagName(), attachmentLocation);
    }
    @Override
    default @NotNull AttachmentCategory getAttachmentCategory(CompoundTag attachmentCustomDataTag) {
        AttachmentCategory category = AttachmentCategory.fromString(NBTUtils.getString(attachmentCustomDataTag, AttachmentProperty.ATTACHMENT_CATEGORY.getTagName()));
        return category != null ? category : AttachmentCategory.NONE;
    }
    @Override
    default void setAttachmentCategory(CompoundTag attachmentCustomDataTag, AttachmentCategory attachmentCategory) {
        NBTUtils.setString(attachmentCustomDataTag, AttachmentProperty.ATTACHMENT_CATEGORY.getTagName(),
                attachmentCategory.getCategoryName()); // 存在配件NBT里用不带前缀的简写
    }

    @Override
    default int getScopeViewIndex(CompoundTag attachmentCustomDataTag) {
        return NBTUtils.getInt(attachmentCustomDataTag, AttachmentProperty.SCOPE_VIEW_INDEX.getTagName());
    }
    @Override
    default void setScopeViewIndex(CompoundTag attachmentCustomDataTag, int scopeViewIndex) {
        NBTUtils.setInt(attachmentCustomDataTag, AttachmentProperty.SCOPE_VIEW_INDEX.getTagName(), scopeViewIndex);
    }

    @Override
    default boolean hasLaserColor(CompoundTag attachmentCustomDataTag) {
        return NBTUtils.hasKey(attachmentCustomDataTag, AttachmentProperty.LASER_COLOR.getTagName());
    }
    @Override
    default int getLaserColor(CompoundTag attachmentCustomDataTag) {
        return NBTUtils.getInt(attachmentCustomDataTag, AttachmentProperty.LASER_COLOR.getTagName());
    }
    @Override
    default void setLaserColor(CompoundTag attachmentCustomDataTag, int laserColor) {
        NBTUtils.setInt(attachmentCustomDataTag, AttachmentProperty.LASER_COLOR.getTagName(), laserColor);
    }
}
