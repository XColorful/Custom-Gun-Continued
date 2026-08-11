/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.attachment;

import dev.xcolorful.customgun.core.api.item.AttachmentProperty;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.util.NBTUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface AttachmentDataAccessor extends AttachmentNBTAccessor, IAttachmentDataAccess {

    // --------IAttachmentDataAccess--------

    @Override
    default @NotNull ResourceLocation getAttachmentLocation(ItemStack attachmentItem) {
        @Nullable var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return ResourceTag.NULL_LOCATION;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getAttachmentLocation(customDataTag);
    }
    @Override
    default void setAttachmentLocation(ItemStack attachmentItem, ResourceLocation attachmentLocation) {
        var customData = NBTUtils.getOrCreateCustomData(attachmentItem);
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setAttachmentLocation(customDataTag, attachmentLocation);
        NBTUtils.setCustomDataTag(attachmentItem, customDataTag);
    }

    @Override
    default @NotNull AttachmentCategory getAttachmentCategory(ItemStack attachmentItem) {
        @Nullable var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return AttachmentCategory.NONE;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getAttachmentCategory(customDataTag);
    }
    @Override
    default void setAttachmentCategory(ItemStack attachmentItem, AttachmentCategory attachmentCategory) {
        var customData = NBTUtils.getOrCreateCustomData(attachmentItem);
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setAttachmentCategory(customDataTag, attachmentCategory);
        NBTUtils.setCustomDataTag(attachmentItem, customDataTag);
    }

    @Override
    default int getScopeViewIndex(ItemStack attachmentItem) {
        @Nullable var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return 0;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getScopeViewIndex(customDataTag);
    }
    @Override
    default void setScopeViewIndex(ItemStack attachmentItem, int scopeViewIndex) {
        var customData = NBTUtils.getOrCreateCustomData(attachmentItem);
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setScopeViewIndex(customDataTag, scopeViewIndex);
        NBTUtils.setCustomDataTag(attachmentItem, customDataTag);
    }

    @Override
    default boolean hasLaserColor(ItemStack attachmentItem) {
        @Nullable var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return false;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.hasLaserColor(customDataTag);
    }
    @Override
    default int getLaserColorInt(ItemStack attachmentItem) {
        @Nullable var customData = NBTUtils.getCustomData(attachmentItem);
        if (customData == null) return 0;
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        return this.getLaserColor(customDataTag);
    }
    @Override
    default void setLaserColorInt(ItemStack attachmentItem, int laserColor) {
        var customData = NBTUtils.getOrCreateCustomData(attachmentItem);
        @NotNull CompoundTag customDataTag = NBTUtils.getCustomDataTag(customData);
        this.setLaserColor(customDataTag, laserColor);
        NBTUtils.setCustomDataTag(attachmentItem, customDataTag);
    }

    // --------IAttachmentStateAccess--------

    @Override
    default boolean hasTooltipMask(ItemStack attachmentItem) {
        return NBTUtils.hasKey(attachmentItem, AttachmentProperty.TOOLTIP_MASK.getTagName());
    }
    @Override
    default int getTooltipMask(ItemStack attachmentItem) {
        return NBTUtils.getInt(attachmentItem, AttachmentProperty.TOOLTIP_MASK.getTagName());
    }
    @Override
    default void setTooltipMask(ItemStack attachmentItem, int tooltipMask) {
        NBTUtils.setInt(attachmentItem, AttachmentProperty.TOOLTIP_MASK.getTagName(), tooltipMask);
    }
}
