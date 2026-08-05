/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.network.message.event;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.util.ClientWorldUtils;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterDrawEvent;
import dev.xcolorful.customgun.core.network.message.event.ServerMessageGunDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class _ServerMessageGunDraw {

    public static void doClientEvent(ServerMessageGunDraw message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;

        @Nullable LivingEntity livingShooter = ClientWorldUtils.getLivingEntityById(level, message.entityId());
        @Nullable ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromEntity(livingShooter);

        ShooterDrawEvent event = new ShooterDrawEvent(McLogicalSide.CLIENT,
                iLivingShooter, livingShooter,
                message.previousGunItem(), message.currentGunItem());
        CustomGun.getEventPoster().postCustomEvent(event);
    }
}
