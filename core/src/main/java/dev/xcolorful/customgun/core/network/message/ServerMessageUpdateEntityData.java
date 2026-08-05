/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message._ServerMessageUpdateEntityData;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.entity.sync.DataEntry;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record ServerMessageUpdateEntityData(int entityId,
                                            List<DataEntry<?, ?>> entries)
        implements IMessage<ServerMessageUpdateEntityData> {

    @Override
    public void encode(ServerMessageUpdateEntityData message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeVarInt(message.entries.size());
        message.entries.forEach(entry -> entry.write(buffer));
    }

    public static ServerMessageUpdateEntityData decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        int size = buffer.readVarInt();
        List<DataEntry<?, ?>> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            entries.add(DataEntry.read(buffer));
        }
        return new ServerMessageUpdateEntityData(entityId, entries);
    }

    @Override
    public void handle(ServerMessageUpdateEntityData message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _ServerMessageUpdateEntityData.onHandle(message));
        }
    }
}