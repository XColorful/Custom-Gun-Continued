/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.player;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.player._S2CMessagePlayerSwapItem;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class S2CMessagePlayerSwapItem implements IMessage<S2CMessagePlayerSwapItem> {

    public S2CMessagePlayerSwapItem() {
    }

    @Override
    public void encode(S2CMessagePlayerSwapItem message, FriendlyByteBuf buffer) {
    }

    public static S2CMessagePlayerSwapItem decode(FriendlyByteBuf buffer) {
        return new S2CMessagePlayerSwapItem();
    }

    @Override
    public void handle(S2CMessagePlayerSwapItem message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _S2CMessagePlayerSwapItem.doClientEvent(message));
        }
    }
}