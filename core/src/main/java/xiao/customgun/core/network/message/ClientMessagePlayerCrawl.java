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

public record ClientMessagePlayerCrawl(boolean isCrawl)
        implements IMessage<ClientMessagePlayerCrawl> {

    @Override
    public void encode(ClientMessagePlayerCrawl message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isCrawl);
    }

    public static ClientMessagePlayerCrawl decode(FriendlyByteBuf buffer) {
        return new ClientMessagePlayerCrawl(buffer.readBoolean());
    }

    @Override
    public void handle(ClientMessagePlayerCrawl message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer entity)) {
                    return;
                }
                // TODO SyncConfig
                // TODO IGunOperator
            });
        }
    }
}