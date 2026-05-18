/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;

public interface IGunAttachmentDataGetter {

    boolean isAttachmentEnabled(ItemStack gunItem, AttachmentCategory attachmentCategory);

    @Nullable CompoundTag getAttachmentCustomDataTag(ItemStack gunItem, AttachmentCategory attachmentCategory);

    /**
     * 获取枪械配件ID，如不存在则返回 {@link IGunAttachmentDataGetter#EMPTY_ATTACHMENT_LOCATION}
     */
    @NotNull ResourceLocation getAttachmentLocation(ItemStack gunItem, AttachmentCategory attachmentCategory);
    ResourceLocation EMPTY_ATTACHMENT_LOCATION = CustomGun.getMcRegistry().createResourceLocation(CustomGun.MOD_ID + ":null");

    // --------Deprecated--------

    @Deprecated default boolean allowAttachmentType(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        return isAttachmentEnabled(gunItem, attachmentCategory);
    }

    @Deprecated default @Nullable CompoundTag getAttachmentTag(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        return getAttachmentCustomDataTag(gunItem, attachmentCategory);
    }

    @Deprecated default @NotNull ResourceLocation getAttachmentId(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        return getAttachmentLocation(gunItem, attachmentCategory);
    }
    @Deprecated default @NotNull ResourceLocation getBuiltInAttachmentId(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        return getAttachmentLocation(gunItem, attachmentCategory);
    }
}
