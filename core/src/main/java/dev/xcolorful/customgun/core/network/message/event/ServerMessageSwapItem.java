/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.event._ServerMessageSwapItem;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class ServerMessageSwapItem implements IMessage<ServerMessageSwapItem> {

    public ServerMessageSwapItem() {
    }

    @Override
    public void encode(ServerMessageSwapItem message, FriendlyByteBuf buffer) {
    }

    public static ServerMessageSwapItem decode(FriendlyByteBuf buffer) {
        return new ServerMessageSwapItem();
    }

    @Override
    public void handle(ServerMessageSwapItem message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _ServerMessageSwapItem.doClientEvent(message));
        }
    }
}