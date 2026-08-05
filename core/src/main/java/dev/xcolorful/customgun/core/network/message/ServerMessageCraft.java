/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message._ServerMessageCraft;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;

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