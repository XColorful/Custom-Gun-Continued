/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message._ServerMessageSyncBaseTimestamp;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class ServerMessageSyncBaseTimestamp implements IMessage<ServerMessageSyncBaseTimestamp> {

    public ServerMessageSyncBaseTimestamp() {
    }

    @Override
    public void encode(ServerMessageSyncBaseTimestamp message, FriendlyByteBuf buffer) {
    }

    public static ServerMessageSyncBaseTimestamp decode(FriendlyByteBuf buffer) {
        return new ServerMessageSyncBaseTimestamp();
    }

    @Override
    public void handle(ServerMessageSyncBaseTimestamp message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            long timestamp = System.currentTimeMillis();
            handler.accept(() -> {
                _ServerMessageSyncBaseTimestamp.updateBaseTimestamp(timestamp);
                context.reply(new ClientMessageSyncBaseTimestamp());
            });
        }
    }
}