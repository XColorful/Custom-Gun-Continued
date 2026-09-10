/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.handshake;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.entity.sync.SyncedDataKey;
import dev.xcolorful.customgun.core.entity.sync.SyncedEntityData;
import dev.xcolorful.customgun.core.network.LoginIndexHolder;
import dev.xcolorful.customgun.core.util.NetworkUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class S2CMessageSyncedEntityDataMapping extends LoginIndexHolder implements IMessage<S2CMessageSyncedEntityDataMapping> {
    public static final Marker HANDSHAKE = MarkerFactory.getMarker(CustomGun.MOD_ID_SHORT + "_handshake");
    public final Map<ResourceLocation, List<Pair<ResourceLocation, Integer>>> keyMap;

    // ↓这会被隐式调用
    public S2CMessageSyncedEntityDataMapping() {
        this.keyMap = new HashMap<>();
    }

    public S2CMessageSyncedEntityDataMapping(Map<ResourceLocation, List<Pair<ResourceLocation, Integer>>> keyMap) {
        this.keyMap = keyMap;
    }

    @Override
    public void encode(S2CMessageSyncedEntityDataMapping message, FriendlyByteBuf buffer) {
        Set<SyncedDataKey<?, ?>> keys = SyncedEntityData.instance().getKeys();
        buffer.writeInt(keys.size());
        keys.forEach(key -> {
            int id = SyncedEntityData.instance().getInternalId(key);
            NetworkUtils.writeResourceLocation(buffer, key.classKey().id());
            NetworkUtils.writeResourceLocation(buffer, key.id());
            buffer.writeVarInt(id);
        });
    }

    public static S2CMessageSyncedEntityDataMapping decode(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        Map<ResourceLocation, List<Pair<ResourceLocation, Integer>>> keyMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            var classId = NetworkUtils.readResourceLocation(buffer);
            var keyId = NetworkUtils.readResourceLocation(buffer);
            int id = buffer.readVarInt();
            keyMap.computeIfAbsent(classId, c -> new ArrayList<>()).add(Pair.of(keyId, id));
        }
        return new S2CMessageSyncedEntityDataMapping(keyMap);
    }

    @Override
    public void handle(S2CMessageSyncedEntityDataMapping message, Consumer<Runnable> handler, NetworkContext context) {
        CustomGun.LOGGER.debug(HANDSHAKE, "Received synced key mappings from server");
        CountDownLatch block = new CountDownLatch(1);
        handler.accept(() -> {
            try {
                if (!SyncedEntityData.instance().updateMappings(message)) {
                    context.connection().disconnect(Component.literal("Connection closed - Received unknown synced data keys."));
                }
            } finally {
                block.countDown();
            }
        });
        try {
            block.await();
        } catch (InterruptedException e) {
            CustomGun.LOGGER.error("Interrupted while waiting for synced key mappings from server", e);
        }
        context.setHandled();
        context.reply(new C2SMessageAcknowledge());
    }

    public Map<ResourceLocation, List<Pair<ResourceLocation, Integer>>> getKeyMap() {
        return this.keyMap;
    }
}