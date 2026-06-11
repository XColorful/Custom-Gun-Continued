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
import xiao.customgun.client.network.message._ServerMessageSyncGunPack;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.resource.network.SyncDataType;
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
            CustomGun.LOGGER.debug("ServerMessageSyncGunPack: Received sync pack message from server");
            Connection connection = context.connection();
            boolean remoteConnection = connection != null && !connection.isMemoryConnection();
            // 客户端侧可以异步解析Pojo，把enqueue的handler传过去
            _ServerMessageSyncGunPack.doSync(message, handler, remoteConnection);
        }
    }
}