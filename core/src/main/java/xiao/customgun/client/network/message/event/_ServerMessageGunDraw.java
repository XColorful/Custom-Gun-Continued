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
import xiao.customgun.core.network.message.event.ServerMessageGunDraw;

@ApiStatus.Internal
public class _ServerMessageGunDraw {

    public static void doClientEvent(ServerMessageGunDraw message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        LivingEntity livingEntity = WorldUtils.getLivingEntityById(level, message.entityId());
        if (livingEntity != null) {
            // TODO GunDrawEvent
        }
    }
}
