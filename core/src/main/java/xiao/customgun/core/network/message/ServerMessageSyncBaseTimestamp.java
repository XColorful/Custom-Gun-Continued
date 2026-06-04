/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import xiao.customgun.CustomGun;
import xiao.customgun.client.network.message._ServerMessageSyncBaseTimestamp;
import xiao.customgun.core.api.network.message.IMessage;

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