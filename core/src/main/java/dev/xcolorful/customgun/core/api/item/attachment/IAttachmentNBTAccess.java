/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.attachment;

import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * 非 ItemStack 形式的 Access，用于枪械NBT的情况
 */
public interface IAttachmentNBTAccess {

    /**
     * 获取配件ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull ResourceLocation getAttachmentLocation(CompoundTag attachmentCustomDataTag);
    void setAttachmentLocation(CompoundTag attachmentCustomDataTag, ResourceLocation location);

    /**
     * 获取配件类型，如无则返回 {@link AttachmentCategory#NONE}
     */
    @NotNull AttachmentCategory getAttachmentCategory(CompoundTag attachmentCustomDataTag);
    void setAttachmentCategory(CompoundTag attachmentCustomDataTag, AttachmentCategory attachmentCategory);

    /**
     * {@link AttachmentDisplay#getScopeViewIndex()}
     */
    int getScopeViewIndex(CompoundTag attachmentCustomDataTag);
    void setScopeViewIndex(CompoundTag attachmentCustomDataTag, int scopeViewIndex);

    /**
     * {@link AttachmentDisplay#getLaserDisplay()}
     */
    boolean hasLaserColor(CompoundTag attachmentCustomDataTag);
    int getLaserColor(CompoundTag attachmentCustomDataTag);
    void setLaserColor(CompoundTag attachmentCustomDataTag, int laserColor);

    // --------Deprecated--------

    @Deprecated static int getZoomNumberFromTag(CompoundTag attachmentCustomDataTag) {
        return AttachmentNBTAccessor.INSTANCE.getScopeViewIndex(attachmentCustomDataTag);
    }
    @Deprecated static void setZoomNumberToTag(CompoundTag attachmentCustomDataTag, int zoomNumber) {
        AttachmentNBTAccessor.INSTANCE.setScopeViewIndex(attachmentCustomDataTag, zoomNumber);
    }
}
