/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.attachment;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.client.resource.assets.display.AttachmentDisplay;
import xiao.customgun.core.api.resource.ResourceTag;

public interface IAttachmentDataAccess extends IAttachmentPojoGetter,
        IAttachmentNBTAccess,
        _IAttachmentPropertyAccess {

    /**
     * 获取配件ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getAttachmentLocation(ItemStack attachmentItem);
    void setAttachmentLocation(ItemStack attachmentItem, Identifier attachmentLocation);

    /**
     * 获取配件类型，如无则返回 {@link AttachmentCategory#NONE}
     */
    @NotNull AttachmentCategory getAttachmentCategory(ItemStack attachmentItem);
    void setAttachmentCategory(ItemStack attachmentItem, AttachmentCategory attachmentCategory);

    /**
     * {@link AttachmentDisplay#getScopeViewIndex()}
     */
    int getScopeViewIndex(ItemStack attachmentItem);
    void setScopeViewIndex(ItemStack attachmentItem, int scopeViewIndex);

    /**
     * {@link AttachmentDisplay#getLaserDisplay()}
     */
    boolean hasLaserColor(ItemStack attachmentItem);
    int getLaserColor(ItemStack attachmentItem);
    void setLaserColor(ItemStack attachmentItem, int laserColor);

    // --------Deprecated--------

    @Deprecated default Identifier getAttachmentId(ItemStack attachmentItem) {
        return getAttachmentLocation(attachmentItem);
    }
    @Deprecated default void setAttachmentId(ItemStack attachmentItem, Identifier attachmentId) {
        setAttachmentLocation(attachmentItem, attachmentId);
    }

    @Deprecated default AttachmentCategory getType(ItemStack attachmentItem) {
        return getAttachmentCategory(attachmentItem);
    }

    @Deprecated default int getZoomNumber(ItemStack attachmentItem) {
        return getScopeViewIndex(attachmentItem);
    }
    @Deprecated default void setZoomNumber(ItemStack attachmentItem, int zoomNumber) {
        setScopeViewIndex(attachmentItem, zoomNumber);
    }

    @Deprecated default boolean hasCustomLaserColor(ItemStack attachmentItem) {
        return hasLaserColor(attachmentItem);
    }
}
