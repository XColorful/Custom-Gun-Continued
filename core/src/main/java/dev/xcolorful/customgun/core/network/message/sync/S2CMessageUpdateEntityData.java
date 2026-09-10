/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.sync;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.sync._S2CMessageUpdateEntityData;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.entity.sync.DataEntry;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record S2CMessageUpdateEntityData(int entityId,
                                         List<DataEntry<?, ?>> entries)
        implements IMessage<S2CMessageUpdateEntityData> {

    @Override
    public void encode(S2CMessageUpdateEntityData message, FriendlyByteBuf buffer) {
        buffer.writeVarInt(message.entityId);
        buffer.writeVarInt(message.entries.size());
        message.entries.forEach(entry -> entry.write(buffer));
    }

    public static S2CMessageUpdateEntityData decode(FriendlyByteBuf buffer) {
        int entityId = buffer.readVarInt();
        int size = buffer.readVarInt();
        List<DataEntry<?, ?>> entries = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            entries.add(DataEntry.read(buffer));
        }
        return new S2CMessageUpdateEntityData(entityId, entries);
    }

    @Override
    public void handle(S2CMessageUpdateEntityData message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> _S2CMessageUpdateEntityData.onHandle(message));
        }
    }
}