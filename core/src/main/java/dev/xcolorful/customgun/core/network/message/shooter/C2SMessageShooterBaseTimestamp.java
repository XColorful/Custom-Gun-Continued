/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.function.Consumer;

public class C2SMessageShooterBaseTimestamp implements IMessage<C2SMessageShooterBaseTimestamp> {
    public static final Marker MARKER = MarkerFactory.getMarker(CustomGun.MOD_ID_SHORT + "_sync_base_timestamp");

    public C2SMessageShooterBaseTimestamp() {
    }

    @Override
    public void encode(C2SMessageShooterBaseTimestamp message, FriendlyByteBuf buffer) {
    }

    public static C2SMessageShooterBaseTimestamp decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterBaseTimestamp();
    }

    @Override
    public void handle(C2SMessageShooterBaseTimestamp message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            long timestamp = System.currentTimeMillis();
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                ShooterProperty shooterProperty = ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$getShooterProperty();
                shooterProperty.serverBaseTimestamp = timestamp;
                CustomGun.LOGGER.debug(MARKER, "Update {} server base timestamp: {}", player.getName().getString(), shooterProperty.serverBaseTimestamp);
            });
        }
    }
}