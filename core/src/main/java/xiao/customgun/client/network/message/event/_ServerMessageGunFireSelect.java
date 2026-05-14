/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.client.util.WorldUtils;
import xiao.customgun.core.network.message.event.ServerMessageGunFireSelect;

@ApiStatus.Internal
public class _ServerMessageGunFireSelect {

    public static void doClientEvent(ServerMessageGunFireSelect message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        LivingEntity shooter = WorldUtils.getLivingEntityById(level, message.shooterId());
        if (shooter != null) {
            // TODO gunFireSelectEvent
        }
    }
}
