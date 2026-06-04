/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message.event;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import xiao.customgun.CustomGun;
import xiao.customgun.client.network.message.event._ServerMessageGunHurt;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.util.NetworkUtils;

import java.util.function.Consumer;

public record ServerMessageGunHurt(int bulletId, int hurtEntityId, int attackerId,
                                   Identifier gunLocation, Identifier gunDisplayLocation,// 细节：Identifier 放同一行
                                   float amount, boolean isHeadShot, float headshotMultiplier)
        implements IMessage<ServerMessageGunHurt> {

    @Override
    public void encode(ServerMessageGunHurt message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.bulletId);
        buffer.writeInt(message.hurtEntityId);
        buffer.writeInt(message.attackerId);
        NetworkUtils.writeResourceLocation(buffer, message.gunLocation);
        NetworkUtils.writeResourceLocation(buffer, message.gunDisplayLocation);
        buffer.writeFloat(message.amount);
        buffer.writeBoolean(message.isHeadShot);
        buffer.writeFloat(message.headshotMultiplier);
    }

    public static ServerMessageGunHurt decode(FriendlyByteBuf buffer) {
        int bulletId = buffer.readInt();
        int hurtEntityId = buffer.readInt();
        int attackerId = buffer.readInt();
        var gunId = NetworkUtils.readResourceLocation(buffer);
        var gunDisplayId = NetworkUtils.readResourceLocation(buffer);
        float amount = buffer.readFloat();
        boolean isHeadShot = buffer.readBoolean();
        float headshotMultiplier = buffer.readFloat();
        return new ServerMessageGunHurt(bulletId, hurtEntityId, attackerId, gunId, gunDisplayId, amount, isHeadShot, headshotMultiplier);
    }

    @Override
    public void handle(ServerMessageGunHurt message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _ServerMessageGunHurt.onHurt(message));
        }
    }
}
