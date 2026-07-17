/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.network.message;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.CustomGun;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.entity.shooter.ILocalShooterGetter;

import java.util.Objects;

@ApiStatus.Internal
public class _ServerMessageSyncBaseTimestamp {
    private static final Marker MARKER = MarkerFactory.getMarker("SYNC_BASE_TIMESTAMP");

    public static void updateBaseTimestamp(long timestamp) {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        LocalShooterProperty localShooterProperty = ILocalShooterGetter.fromLocalPlayer(player).cgc$getLocalShooterProperty();
        localShooterProperty.clientBaseTimestamp = timestamp;
        CustomGun.LOGGER.debug(MARKER, "Update Client base timestamp: {}", localShooterProperty.clientBaseTimestamp);
    }
}
