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
import xiao.customgun.client.network.message.event._ServerMessageGunShoot;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.util.NetworkUtils;

import java.util.function.Consumer;

public record ServerMessageGunShoot(int shooterId,
                                    ItemStack gunItem)
        implements IMessage<ServerMessageGunShoot> {

    @Override
    public void encode(ServerMessageGunShoot message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.shooterId);
        NetworkUtils.writeItem(buffer, message.gunItem);
    }

    public static ServerMessageGunShoot decode(FriendlyByteBuf buffer) {
        int shooterId = buffer.readVarInt();
        ItemStack gunItem = NetworkUtils.readItem(buffer);
        return new ServerMessageGunShoot(shooterId, gunItem);
    }

    @Override
    public void handle(ServerMessageGunShoot message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _ServerMessageGunShoot.doClientEvent(message));
        }
    }
}