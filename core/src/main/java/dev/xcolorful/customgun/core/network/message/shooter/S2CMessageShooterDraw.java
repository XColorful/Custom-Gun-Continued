/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.shooter._S2CMessageShooterDraw;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public record S2CMessageShooterDraw(int entityId,
                                    ItemStack previousGunItem,
                                    ItemStack currentGunItem)
        implements IMessage<S2CMessageShooterDraw> {

    @Override
    public void encode(S2CMessageShooterDraw message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        NetworkUtils.writeItem(buffer, message.previousGunItem);
        NetworkUtils.writeItem(buffer, message.currentGunItem);
    }

    public static S2CMessageShooterDraw decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        ItemStack previousGunItem = NetworkUtils.readItem(buffer);
        ItemStack currentGunItem = NetworkUtils.readItem(buffer);
        return new S2CMessageShooterDraw(entityId, previousGunItem, currentGunItem);
    }

    @Override
    public void handle(S2CMessageShooterDraw message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _S2CMessageShooterDraw.doClientEvent(message));
        }
    }
}