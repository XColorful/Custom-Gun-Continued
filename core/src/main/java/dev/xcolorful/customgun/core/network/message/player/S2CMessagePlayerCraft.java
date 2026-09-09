/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.player;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.player._S2CMessagePlayerCraft;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public record S2CMessagePlayerCraft(int menuId)
        implements IMessage<S2CMessagePlayerCraft> {

    @Override
    public void encode(S2CMessagePlayerCraft message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.menuId);
    }

    public static S2CMessagePlayerCraft decode(FriendlyByteBuf buffer) {
        return new S2CMessagePlayerCraft(buffer.readVarInt());
    }

    @Override
    public void handle(S2CMessagePlayerCraft message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> {
                CustomGun.getSideExecutor().executeOn(McSide.CLIENT, () -> () ->
                        _S2CMessagePlayerCraft.updateScreen(message.menuId)
                );
            });
        }
    }
}