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
import xiao.customgun.core.api.resource.ResourceTag;

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

    int getScopeViewIndex(CompoundTag attachmentCustomDataTag);
}
