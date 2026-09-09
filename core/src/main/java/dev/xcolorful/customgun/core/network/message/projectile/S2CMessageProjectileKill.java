/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.projectile;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.projectile._S2CMessageProjectileKill;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public record S2CMessageProjectileKill(int bulletId, int victimEntityId, int shooterId,
                                       ResourceLocation gunLocation, ResourceLocation gunDisplayLocation,// 细节：Identifier 放同一行
                                       float baseDamage, boolean isHeadShot, float headshotMultiplier)
        implements IMessage<S2CMessageProjectileKill> {

    @Override
    public void encode(S2CMessageProjectileKill message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.bulletId);
        buffer.writeInt(message.victimEntityId);
        buffer.writeInt(message.shooterId);
        NetworkUtils.writeResourceLocation(buffer, message.gunLocation);
        NetworkUtils.writeResourceLocation(buffer, message.gunDisplayLocation);
        buffer.writeFloat(message.baseDamage);
        buffer.writeBoolean(message.isHeadShot);
        buffer.writeFloat(message.headshotMultiplier);
    }

    public static S2CMessageProjectileKill decode(FriendlyByteBuf buffer) {
        int bulletId = buffer.readInt();
        int victimEntityId = buffer.readInt();
        int shooterId = buffer.readInt();
        var gunLocation = NetworkUtils.readResourceLocation(buffer);
        var gunDisplayLocation = NetworkUtils.readResourceLocation(buffer);
        float baseDamage = buffer.readFloat();
        boolean isHeadShot = buffer.readBoolean();
        float headshotMultiplier = buffer.readFloat();
        return new S2CMessageProjectileKill(bulletId, victimEntityId, shooterId, gunLocation, gunDisplayLocation, baseDamage, isHeadShot, headshotMultiplier);
    }

    @Override
    public void handle(S2CMessageProjectileKill message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _S2CMessageProjectileKill.onKill(message));
        }
    }
}