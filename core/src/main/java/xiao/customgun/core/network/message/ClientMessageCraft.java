/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.util.NetworkUtils;

import java.util.function.Consumer;

public record ClientMessageCraft(
        Identifier recipeId, // 细节：Identifier 放同一行
        int menuId)
        implements IMessage<ClientMessageCraft> {

    @Override
    public void encode(ClientMessageCraft message, FriendlyByteBuf buffer) {
        NetworkUtils.writeResourceLocation(buffer, message.recipeId);
        buffer.writeVarInt(message.menuId);
    }

    public static ClientMessageCraft decode(FriendlyByteBuf buffer) {
        return new ClientMessageCraft(NetworkUtils.readResourceLocation(buffer), buffer.readVarInt());
    }

    @Override
    public void handle(ClientMessageCraft message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer entity)) {
                    return;
                }
                // TODO GunSmithTableMenu
            });
        }
    }
}