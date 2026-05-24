/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message.handshake;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.entity.sync.SyncedDataKey;
import xiao.customgun.core.entity.sync.SyncedEntityData;
import xiao.customgun.core.network.LoginIndexHolder;
import xiao.customgun.core.util.NetworkUtils;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class ServerMessageSyncedEntityDataMapping extends LoginIndexHolder implements IMessage<ServerMessageSyncedEntityDataMapping> {
    public static final Marker HANDSHAKE = MarkerFactory.getMarker("TACZ_HANDSHAKE");
    public final Map<ResourceLocation, List<Pair<ResourceLocation, Integer>>> keyMap;

    // ↓这会被隐式调用
    public ServerMessageSyncedEntityDataMapping() {
        this.keyMap = new HashMap<>();
    }

    public ServerMessageSyncedEntityDataMapping(Map<ResourceLocation, List<Pair<ResourceLocation, Integer>>> keyMap) {
        this.keyMap = keyMap;
    }

    @Override
    public void encode(ServerMessageSyncedEntityDataMapping message, FriendlyByteBuf buffer) {
        Set<SyncedDataKey<?, ?>> keys = SyncedEntityData.instance().getKeys();
        buffer.writeInt(keys.size());
        keys.forEach(key -> {
            int id = SyncedEntityData.instance().getInternalId(key);
            NetworkUtils.writeResourceLocation(buffer, key.classKey().id());
            NetworkUtils.writeResourceLocation(buffer, key.id());
            buffer.writeVarInt(id);
        });
    }

    public static ServerMessageSyncedEntityDataMapping decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Map<ResourceLocation, List<Pair<ResourceLocation, Integer>>> keyMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            var classId = NetworkUtils.readResourceLocation(buffer);
            var keyId = NetworkUtils.readResourceLocation(buffer);
            int id = buffer.readVarInt();
            keyMap.computeIfAbsent(classId, c -> new ArrayList<>()).add(Pair.of(keyId, id));
        }
        return new ServerMessageSyncedEntityDataMapping(keyMap);
    }

    @Override
    public void handle(ServerMessageSyncedEntityDataMapping message, Consumer<Runnable> handler, NetworkContext context) {
        CustomGun.LOGGER.debug(HANDSHAKE, "Received synced key mappings from server");
        CountDownLatch block = new CountDownLatch(1);
        handler.accept(() -> {
            try {
                if (!SyncedEntityData.instance().updateMappings(message)) {
                    context.connection().disconnect(Component.literal("Connection closed - [TacZ] Received unknown synced data keys."));
                }
            } finally {
                block.countDown();
            }
        });
        try {
            block.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        context.setHandled();
        context.reply(new Acknowledge());
    }

    public Map<ResourceLocation, List<Pair<ResourceLocation, Integer>>> getKeyMap() {
        return this.keyMap;
    }
}