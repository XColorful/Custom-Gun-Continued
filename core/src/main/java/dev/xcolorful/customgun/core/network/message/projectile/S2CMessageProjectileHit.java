/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.projectile;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.projectile._S2CMessageProjectileHit;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public record S2CMessageProjectileHit(int bulletId, int victimEntityId, int shooterId,
                                      Identifier gunLocation, Identifier gunDisplayLocation,// 细节：Identifier 放同一行
                                      float damage, boolean isHeadShot, float headshotMultiplier)
        implements IMessage<S2CMessageProjectileHit> {

    @Override
    public void encode(S2CMessageProjectileHit message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.bulletId);
        buffer.writeInt(message.victimEntityId);
        buffer.writeInt(message.shooterId);
        NetworkUtils.writeResourceLocation(buffer, message.gunLocation);
        NetworkUtils.writeResourceLocation(buffer, message.gunDisplayLocation);
        buffer.writeFloat(message.damage);
        buffer.writeBoolean(message.isHeadShot);
        buffer.writeFloat(message.headshotMultiplier);
    }

    public static S2CMessageProjectileHit decode(FriendlyByteBuf buffer) {
        int bulletId = buffer.readInt();
        int victimEntityId = buffer.readInt();
        int shooterId = buffer.readInt();
        var gunId = NetworkUtils.readResourceLocation(buffer);
        var gunDisplayId = NetworkUtils.readResourceLocation(buffer);
        float damage = buffer.readFloat();
        boolean isHeadShot = buffer.readBoolean();
        float headshotMultiplier = buffer.readFloat();
        return new S2CMessageProjectileHit(bulletId, victimEntityId, shooterId, gunId, gunDisplayId, damage, isHeadShot, headshotMultiplier);
    }

    @Override
    public void handle(S2CMessageProjectileHit message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _S2CMessageProjectileHit.onHurt(message));
        }
    }
}
