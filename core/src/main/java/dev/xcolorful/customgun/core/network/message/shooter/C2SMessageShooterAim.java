/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public record C2SMessageShooterAim(boolean isAim)
        implements IMessage<C2SMessageShooterAim> {

    @Override
    public void encode(C2SMessageShooterAim message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isAim);
    }

    public static C2SMessageShooterAim decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterAim(buffer.readBoolean());
    }

    @Override
    public void handle(C2SMessageShooterAim message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$aim(message.isAim);
            });
        }
    }
}