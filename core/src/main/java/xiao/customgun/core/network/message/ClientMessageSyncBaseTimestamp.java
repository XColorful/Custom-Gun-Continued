/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.network.message.IMessage;

import java.util.function.Consumer;

public class ClientMessageSyncBaseTimestamp implements IMessage<ClientMessageSyncBaseTimestamp> {

    public ClientMessageSyncBaseTimestamp() {
    }

    @Override
    public void encode(ClientMessageSyncBaseTimestamp message, FriendlyByteBuf buffer) {
    }

    public static ClientMessageSyncBaseTimestamp decode(FriendlyByteBuf buffer) {
        return new ClientMessageSyncBaseTimestamp();
    }

    @Override
    public void handle(ClientMessageSyncBaseTimestamp message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            long timestamp = System.currentTimeMillis();
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer entity)) {
                    return;
                }
                // TODO IGunOperator
                // TODO ShooterDataHolder
                // TODO Marker
            });
        }
    }
}