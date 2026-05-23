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
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.index.AttachmentIndex;
import xiao.customgun.core.util.NBTUtils;

public interface AttachmentDataAccessor extends AttachmentNBTAccessor, IAttachmentDataAccess {

    // --------IAttachmentDataAccess--------

    @Override
    default @NotNull ResourceLocation getAttachmentLocation(ItemStack attachmentItem) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return ResourceTag.NULL_LOCATION;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getAttachmentLocation(customDataTag);
    }
    @Override
    default void setAttachmentLocation(ItemStack attachmentItem, ResourceLocation attachmentLocation) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setAttachmentLocation(customDataTag, attachmentLocation);
        NBTUtils.setCustomDataTag(attachmentItem, customDataTag);
    }

    @Override
    default @NotNull AttachmentCategory getAttachmentCategory(ItemStack attachmentItem) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return AttachmentCategory.NONE;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getAttachmentCategory(customDataTag);
    }
    @Override
    default void setAttachmentCategory(ItemStack attachmentItem, AttachmentCategory attachmentCategory) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setAttachmentCategory(customDataTag, attachmentCategory);
    }

    @Override
    default int getScopeViewIndex(ItemStack attachmentItem) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return 0;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getScopeViewIndex(customDataTag);
    }
    @Override
    default void setScopeViewIndex(ItemStack attachmentItem, int scopeViewIndex) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setScopeViewIndex(customDataTag, scopeViewIndex);
        NBTUtils.setCustomDataTag(attachmentItem, customDataTag);
    }

    @Override
    default boolean hasLaserColor(ItemStack attachmentItem) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return false;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.hasLaserColor(customDataTag);
    }
    @Override
    default int getLaserColor(ItemStack attachmentItem) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return 0;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getLaserColor(customDataTag);
    }
    @Override
    default void setLaserColor(ItemStack attachmentItem, int laserColor) {
        var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setLaserColor(customDataTag, laserColor);
        NBTUtils.setCustomDataTag(attachmentItem, customDataTag);
    }

    // --------IAttachmentPojoGetter--------

    @Override
    default @Nullable AttachmentIndex getAttachmentIndex(ItemStack attachmentItem) {
        var indexLocation = this.getAttachmentLocation(attachmentItem);
        return ResourceApi.getAttachmentIndex(indexLocation);
    }
    @Override
    default @Nullable AttachmentData getAttachmentData(ItemStack attachmentItem) {
        @Nullable AttachmentIndex attachmentIndex = this.getAttachmentIndex(attachmentItem);
        if (attachmentIndex == null) return null;
        return ResourceApi.getAttachmentData(attachmentIndex.getDataLocation());
    }
}
