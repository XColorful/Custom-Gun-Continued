/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.event._ServerMessageGunShoot;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public record S2CMessageShooterFire(int shooterId,
                                    ItemStack gunItem)
        implements IMessage<S2CMessageShooterFire> {

    @Override
    public void encode(S2CMessageShooterFire message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.shooterId);
        NetworkUtils.writeItem(buffer, message.gunItem);
    }

    public static S2CMessageShooterFire decode(FriendlyByteBuf buffer) {
        int shooterId = buffer.readVarInt();
        ItemStack gunItem = NetworkUtils.readItem(buffer);
        return new S2CMessageShooterFire(shooterId, gunItem);
    }

    @Override
    public void handle(S2CMessageShooterFire message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _ServerMessageGunShoot.doClientEvent(message));
        }
    }
}