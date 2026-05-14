/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message.event;

import xiao.customgun.CustomGun;
import xiao.customgun.client.network.message.event._ServerMessageGunDraw;
import xiao.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.util.NetworkUtils;

import java.util.function.Consumer;

public record ServerMessageGunDraw(int entityId,
                                   ItemStack previousGunItem,
                                   ItemStack currentGunItem)
        implements IMessage<ServerMessageGunDraw> {

    @Override
    public void encode(ServerMessageGunDraw message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        NetworkUtils.writeItem(buffer, message.previousGunItem);
        NetworkUtils.writeItem(buffer, message.currentGunItem);
    }

    public static ServerMessageGunDraw decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        ItemStack previousGunItem = NetworkUtils.readItem(buffer);
        ItemStack currentGunItem = NetworkUtils.readItem(buffer);
        return new ServerMessageGunDraw(entityId, previousGunItem, currentGunItem);
    }

    @Override
    public void handle(ServerMessageGunDraw message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _ServerMessageGunDraw.doClientEvent(message));
        }
    }
}