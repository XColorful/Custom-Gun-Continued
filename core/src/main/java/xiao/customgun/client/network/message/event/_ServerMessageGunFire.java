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
import xiao.customgun.client.util.ClientWorldUtils;
import xiao.customgun.core.network.message.event.ServerMessageGunFire;

@ApiStatus.Internal
public class _ServerMessageGunFire {

    public static void doClientEvent(ServerMessageGunFire message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        LivingEntity shooter = ClientWorldUtils.getLivingEntityById(level, message.shooterId());
        if (shooter != null) {
            // TODO GunFireEvent
        }
    }
}
