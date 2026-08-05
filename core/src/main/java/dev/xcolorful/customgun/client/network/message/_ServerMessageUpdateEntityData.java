/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.network.message;

import dev.xcolorful.customgun.client.util.ClientWorldUtils;
import dev.xcolorful.customgun.core.entity.sync.SyncedEntityData;
import dev.xcolorful.customgun.core.network.message.ServerMessageUpdateEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class _ServerMessageUpdateEntityData {

    public static void onHandle(ServerMessageUpdateEntityData message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity entity = ClientWorldUtils.getEntityById(level, message.entityId());
        if (entity == null) {
            return;
        }
        SyncedEntityData instance = SyncedEntityData.instance();
        message.entries().forEach(entry -> instance.set(entity, entry.getKey(), entry.getValue()));
    }
}
