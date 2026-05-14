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
import xiao.customgun.client.network.message.event._ServerMessageGunFire;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.util.NetworkUtils;

import java.util.function.Consumer;

public record ServerMessageGunFire(int shooterId,
                                   ItemStack gunItemStack)
        implements IMessage<ServerMessageGunFire> {

    @Override
    public void encode(ServerMessageGunFire message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.shooterId);
        NetworkUtils.writeItem(buffer, message.gunItemStack);
    }

    public static ServerMessageGunFire decode(FriendlyByteBuf buffer) {
        int shooterId = buffer.readVarInt();
        ItemStack gunItemStack = NetworkUtils.readItem(buffer);
        return new ServerMessageGunFire(shooterId, gunItemStack);
    }

    @Override
    public void handle(ServerMessageGunFire message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _ServerMessageGunFire.doClientEvent(message));
        }
    }
}