/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message.event;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.CustomGun;
import xiao.customgun.client.network.message.event._ServerMessageGunMelee;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.util.NetworkUtils;

import java.util.function.Consumer;

public record ServerMessageGunMelee(int shooterId,
                                    ItemStack gunItemStack)
        implements IMessage<ServerMessageGunMelee> {

    @Override
    public void encode(ServerMessageGunMelee message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.shooterId);
        NetworkUtils.writeItem(buffer, message.gunItemStack);
    }

    public static ServerMessageGunMelee decode(FriendlyByteBuf buffer) {
        int shooterId = buffer.readVarInt();
        ItemStack gunItemStack = NetworkUtils.readItem(buffer);
        return new ServerMessageGunMelee(shooterId, gunItemStack);
    }

    @Override
    public void handle(ServerMessageGunMelee message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _ServerMessageGunMelee.doClientEvent(message));
        }
    }
}