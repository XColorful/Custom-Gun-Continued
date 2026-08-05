/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message._ServerMessageLevelUp;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public record ServerMessageLevelUp(ItemStack gun,
                                   int level)
        implements IMessage<ServerMessageLevelUp> {

    @Override
    public void encode(ServerMessageLevelUp message, FriendlyByteBuf buffer) {
        NetworkUtils.writeItem(buffer, message.gun);
        buffer.writeVarInt(message.level);
    }

    public static ServerMessageLevelUp decode(FriendlyByteBuf buffer) {
        ItemStack gun = NetworkUtils.readItem(buffer);
        int level = buffer.readInt();
        return new ServerMessageLevelUp(gun, level);
    }

    @Override
    public void handle(ServerMessageLevelUp message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> {
                CustomGun.getSideExecutor().executeOn(McSide.CLIENT, () -> () ->
                        _ServerMessageLevelUp.onLevelUp(message)
                );
            });
        }
    }
}