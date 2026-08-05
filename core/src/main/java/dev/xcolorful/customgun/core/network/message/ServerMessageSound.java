/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message._ServerMessageSound;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public record ServerMessageSound(int entityId,
                                 Identifier gunLocation, Identifier gunDisplayLocation,// 细节：Identifier 放同一行
                                 String soundName, float volume, float pitch, int distance)
        implements IMessage<ServerMessageSound> {

    @Override
    public void encode(ServerMessageSound message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        NetworkUtils.writeResourceLocation(buffer, message.gunLocation);
        NetworkUtils.writeResourceLocation(buffer, message.gunDisplayLocation);
        NetworkUtils.writeUtf(buffer, message.soundName);
        buffer.writeFloat(message.volume);
        buffer.writeFloat(message.pitch);
        buffer.writeVarInt(message.distance);
    }

    public static ServerMessageSound decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        var gunId = NetworkUtils.readResourceLocation(buffer);
        var gunDisplayId = NetworkUtils.readResourceLocation(buffer);
        String soundName = NetworkUtils.readUtf(buffer);
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        int distance = buffer.readInt();
        return new ServerMessageSound(entityId, gunId, gunDisplayId, soundName, volume, pitch, distance);
    }

    @Override
    public void handle(ServerMessageSound message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> {
                CustomGun.getSideExecutor().executeOn(McSide.CLIENT, () -> () ->
                        _ServerMessageSound.playSound(message)
                );
            });
        }
    }
}