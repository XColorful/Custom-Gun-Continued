/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.resource.SyncDataType;
import xiao.customgun.client.network.message._ServerMessageSyncGunPack;
import xiao.customgun.core.util.NetworkUtils;

import java.util.Map;
import java.util.function.Consumer;

public record ServerMessageSyncGunPack(Map<SyncDataType, Map<ResourceLocation, String>> cache)
        implements IMessage<ServerMessageSyncGunPack> {

    @Override
    public void encode(ServerMessageSyncGunPack message, FriendlyByteBuf buffer) {
        NetworkUtils.writeEnumMap(buffer, message.cache(), NetworkUtils::writeResourceLocationMap);
    }

    public static ServerMessageSyncGunPack decode(FriendlyByteBuf buffer) {
        var map = NetworkUtils.readEnumMap(buffer, SyncDataType.class, NetworkUtils::readResourceLocationMap);
        return new ServerMessageSyncGunPack(map);
    }

    @Override
    public void handle(ServerMessageSyncGunPack message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            Connection connection = context.connection();
            boolean remoteConnection = connection != null && !connection.isMemoryConnection();
            handler.accept(() -> {
                _ServerMessageSyncGunPack.doSync(message, remoteConnection);
            });
        }
    }
}