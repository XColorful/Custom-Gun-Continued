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

public class ClientMessagePlayerDrawGun implements IMessage<ClientMessagePlayerDrawGun> {

    public ClientMessagePlayerDrawGun() {
    }

    @Override
    public void encode(ClientMessagePlayerDrawGun message, FriendlyByteBuf buffer) {
    }

    public static ClientMessagePlayerDrawGun decode(FriendlyByteBuf buffer) {
        return new ClientMessagePlayerDrawGun();
    }

    @Override
    public void handle(ClientMessagePlayerDrawGun message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer entity)) {
                    return;
                }
                // TODO Inventory
                // TODO IGunOperator
            });
        }
    }
}