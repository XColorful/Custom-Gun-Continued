/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.client.network.message._ServerMessageUpdateEntityData;
import xiao.customgun.core.entity.sync.core.DataEntry;

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