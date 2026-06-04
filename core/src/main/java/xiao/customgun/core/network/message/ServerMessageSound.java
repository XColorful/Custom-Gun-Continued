/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.CustomGun;
import xiao.customgun.client.network.message._ServerMessageSound;
import xiao.customgun.core.api.common.McSide;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.util.NetworkUtils;

import java.util.function.Consumer;

public record ServerMessageSound(int entityId,
                                 ResourceLocation gunLocation, ResourceLocation gunDisplayLocation,// 细节：Identifier 放同一行
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