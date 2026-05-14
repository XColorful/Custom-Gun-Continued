/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.network.message.IMessage;

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
            handler.accept(() -> {
                // TODO SwapItemWithOffHand
            });
        }
    }
}