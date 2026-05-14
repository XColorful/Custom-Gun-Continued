/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.util.WorldUtils;
import xiao.customgun.core.network.message.event.ServerMessageGunKill;

@ApiStatus.Internal
public class _ServerMessageGunKill {

    public static void onKill(ServerMessageGunKill message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        @Nullable Entity bullet = WorldUtils.getEntityById(level, message.bulletId());
        @Nullable LivingEntity killedEntity = WorldUtils.getLivingEntityById(level, message.killEntityId());
        @Nullable LivingEntity attacker = WorldUtils.getLivingEntityById(level, message.attackerId());
        // TODO EntityKillByGunEvent
    }
}
