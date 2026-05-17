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
import xiao.customgun.client.util.ClientWorldUtils;
import xiao.customgun.core.network.message.event.ServerMessageGunHurt;

@ApiStatus.Internal
public class _ServerMessageGunHurt {

    public static void onHurt(ServerMessageGunHurt message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        @Nullable Entity bullet = ClientWorldUtils.getEntityById(level, message.bulletId());
        @Nullable Entity hurtEntity = ClientWorldUtils.getEntityById(level, message.hurtEntityId());
        @Nullable LivingEntity attacker = ClientWorldUtils.getLivingEntityById(level, message.attackerId());
        // TODO EntityHurtByGunEvent
    }
}
