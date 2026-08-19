/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.entity.shooter.modifier.ShooterGunModifierManager;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record ClientMessageRefitGun(int attachmentSlotIndex,
                                    int gunSlotIndex,
                                    AttachmentCategory attachmentCategory)
        implements IMessage<ClientMessageRefitGun> {

    @Override
    public void encode(ClientMessageRefitGun message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.attachmentSlotIndex);
        buffer.writeInt(message.gunSlotIndex);
        buffer.writeEnum(message.attachmentCategory);
    }

    public static ClientMessageRefitGun decode(FriendlyByteBuf buffer) {
        return new ClientMessageRefitGun(buffer.readInt(), buffer.readInt(), buffer.readEnum(AttachmentCategory.class));
    }

    @Override
    public void handle(ClientMessageRefitGun message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }
                Inventory inventory = player.getInventory();

                ItemStack gunItem = inventory.getItem(message.gunSlotIndex);
                @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
                if (iGun == null) return;

                // 服务端校验配件锁
                if (iGun.hasAttachmentLock(gunItem)) {
                    return;
                }

                // 从背包取出配件物品
                ItemStack attachmentItem = inventory.getItem(message.attachmentSlotIndex);

                // 检查是否是配件
                @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
                if (iAttachment == null) return;

                // 客户端预期的类型是否与真实的一致
                AttachmentCategory attachmentCategory = iAttachment.getAttachmentCategory(attachmentItem);
                if (attachmentCategory != message.attachmentCategory) {
                    // 不一致就不操作
                    CustomGun.LOGGER.debug("ClientMessageRefitGun: {} (UUID: {}) send invalid attachmentCategory: {} (actual: {})", player.getName(), player.getUUID(), message.attachmentCategory, attachmentCategory);
                    return;
                }

                // 先创建旧配件物品
                ItemStack oldAttachmentItem = iGun.getAttachment(gunItem, attachmentCategory);
                { // 安装配件
                    // 尝试安装 (含校验)
                    if (!iGun.installAttachment(gunItem, attachmentItem)) {
                        return;
                    }
                    // 安装完后立即把原背包物品位置清空
                    inventory.setItem(message.attachmentSlotIndex, ItemStack.EMPTY);
                }

                // 吐出配件物品 (进背包或世界)
                if (!oldAttachmentItem.isEmpty()) {
                    // 尝试将卸下的配件返还到背包
                    if (!inventory.add(oldAttachmentItem)) {
                        // 添加不了就尝试吐出物品实体
                        if (PlannedRefactor.ON_DROP_ITEM_ENTITY_INSTEAD) return;
                        // 尝试吐出物品实体
                        boolean success = true;

                        // 还是失败就覆盖原位置
                        if (!success) {
                            inventory.setItem(message.attachmentSlotIndex, oldAttachmentItem);
                        }
                    }
                }

                // 如果配件是弹匣，吐出所有子弹
                if (attachmentCategory == AttachmentCategory.MAGAZINE) {
                    iGun.unloadAmmo(player, gunItem);
                }

                // 刷新配件数据
                ShooterGunModifierManager.postChangeEvent(player, gunItem);

                player.inventoryMenu.broadcastChanges();
                SendUtils.sendMessageToPlayer(player, new ServerMessageRefreshRefitScreen());
            });
        }
    }
}