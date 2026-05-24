/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import xiao.customgun.CustomGun;
import xiao.customgun.client.network.message._ServerMessageCraft;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.network.message.IMessage;

import java.util.function.Consumer;

public record ServerMessageCraft(int menuId)
        implements IMessage<ServerMessageCraft> {

    @Override
    public void encode(ServerMessageCraft message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.menuId);
    }

    public static ServerMessageCraft decode(FriendlyByteBuf buffer) {
        return new ServerMessageCraft(buffer.readVarInt());
    }

    @Override
    public void handle(ServerMessageCraft message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> {
                CustomGun.getSideExecutor().executeOn(McSide.CLIENT, () -> () ->
                        _ServerMessageCraft.updateScreen(message.menuId)
                );
            });
        }
    }
}