/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.entity.shooter.modifier.ShooterGunModifierManager;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public record ClientMessageUnloadAttachment(int gunSlotIndex,
                                            AttachmentCategory attachmentCategory)
        implements IMessage<ClientMessageUnloadAttachment> {

    @Override
    public void encode(ClientMessageUnloadAttachment message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.gunSlotIndex);
        buffer.writeEnum(message.attachmentCategory);
    }

    public static ClientMessageUnloadAttachment decode(FriendlyByteBuf buffer) {
        return new ClientMessageUnloadAttachment(buffer.readInt(), buffer.readEnum(AttachmentCategory.class));
    }

    @Override
    public void handle(ClientMessageUnloadAttachment message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }
                Inventory inventory = player.getInventory();
                ItemStack gunItem = inventory.getItem(message.gunSlotIndex);
                IGun iGun = IGunGetter.fromItemStack(gunItem);
                if (iGun == null) return;

                // 服务端校验配件锁
                if (iGun.hasAttachmentLock(gunItem)) {
                    return;
                }

                // 吐出配件物品 (进背包或世界)
                {
                    // 先创建卸载的配件物品
                    ItemStack attachmentItem = iGun.getAttachment(gunItem, message.attachmentCategory);
                    if (attachmentItem.isEmpty()) return;

                    // 先尝试将配件添加到背包
                    if (!inventory.add(attachmentItem)) {
                        // 添加不了就尝试吐出物品实体
                        if (PlannedRefactor.ON_DROP_ITEM_ENTITY_INSTEAD) return;
                        // 尝试吐出物品实体
                        boolean success = true;
                        if (!success) return;
                    }
                }

                // 移除配件NBT
                iGun.removeAttachment(gunItem, message.attachmentCategory);

                // 如果配件是弹匣，吐出所有子弹
                if (message.attachmentCategory == AttachmentCategory.MAGAZINE) {
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