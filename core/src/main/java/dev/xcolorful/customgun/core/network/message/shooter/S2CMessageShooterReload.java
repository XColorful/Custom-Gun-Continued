/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.event._ServerMessageGunReload;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public record S2CMessageShooterReload(int shooterId,
                                      ItemStack gunItem)
        implements IMessage<S2CMessageShooterReload> {

    @Override
    public void encode(S2CMessageShooterReload message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.shooterId);
        NetworkUtils.writeItem(buffer, message.gunItem);
    }

    public static S2CMessageShooterReload decode(FriendlyByteBuf buffer) {
        int shooterId = buffer.readVarInt();
        ItemStack gunItem = NetworkUtils.readItem(buffer);
        return new S2CMessageShooterReload(shooterId, gunItem);
    }

    @Override
    public void handle(S2CMessageShooterReload message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _ServerMessageGunReload.doClientEvent(message));
        }
    }
}