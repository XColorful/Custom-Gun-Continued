/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.network.message.IMessage;

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
                // TODO IGun
                // TODO AttachmentPropertyManager
                // TODO ServerMessageRefreshRefitScreen
            });
        }
    }
}