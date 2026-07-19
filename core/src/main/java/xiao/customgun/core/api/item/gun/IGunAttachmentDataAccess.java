/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.resource.ResourceTag;
import xiao.customgun.core.resource.data.data.GunData;

public interface IGunAttachmentDataAccess {

    boolean isAttachmentEnabled(ItemStack gunItem, AttachmentCategory attachmentCategory);
    boolean canInstallAttachment(ItemStack gunItem, ItemStack attachmentItem);

    /**
     * 返回配件物品，如无则返回 {@link ItemStack#EMPTY}
     */
    @NotNull ItemStack getAttachment(ItemStack gunItem, AttachmentCategory attachmentCategory);
    /**
     * 返回默认配件的物品，如无则返回 {@link ItemStack#EMPTY}
     */
    @NotNull ItemStack getBuiltinAttachment(ItemStack gunItem, AttachmentCategory attachmentCategory);

    /**
     * 1.21.1+涉及Tag复制
     * 写入操作需要用{@link #setAttachmentCustomDataTag}保存
     */
    @Nullable CompoundTag getAttachmentCustomDataTag(ItemStack gunItem, AttachmentCategory attachmentCategory);
    /**
     * 不包含检查
     * 只能在{@link #getAttachmentCustomDataTag}后调用
     */
    @ApiStatus.Internal
    void setAttachmentCustomDataTag(ItemStack gunItem, AttachmentCategory attachmentCategory, CompoundTag attachmentCustomDataTag);

    /**
     * 获取枪械配件ID，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getAttachmentLocation(ItemStack gunItem, AttachmentCategory attachmentCategory);
    /**
     * 获取 {@link GunData#getBuiltinAttachments()}，如不存在则返回 {@link ResourceTag#NULL_LOCATION}
     */
    @NotNull Identifier getBuiltinAttachmentLocation(ItemStack gunItem, AttachmentCategory attachmentCategory);

    /**
     * 覆盖安装配件
     * @return 是否安装成功
     */
    boolean installAttachment(ItemStack gunItem, ItemStack attachmentItem);
    /**
     * 移除配件，如需获取配件物品则需要先{@link IGunAttachmentDataAccess#getAttachment}
     */
    void removeAttachment(ItemStack gunItem, AttachmentCategory attachmentCategory);

    // --------Deprecated--------

    @Deprecated default boolean allowAttachmentType(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        return isAttachmentEnabled(gunItem, attachmentCategory);
    }
    @Deprecated default boolean allowAttachment(ItemStack gunItem, ItemStack attachmentItem) {
        return canInstallAttachment(gunItem, attachmentItem);
    }

    @Deprecated default @Nullable CompoundTag getAttachmentTag(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        return getAttachmentCustomDataTag(gunItem, attachmentCategory);
    }

    @Deprecated default @NotNull Identifier getAttachmentId(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        return getAttachmentLocation(gunItem, attachmentCategory);
    }
    @Deprecated default @NotNull Identifier getBuiltInAttachmentId(ItemStack gunItem, AttachmentCategory attachmentCategory) {
        return getBuiltinAttachmentLocation(gunItem, attachmentCategory);
    }
}
