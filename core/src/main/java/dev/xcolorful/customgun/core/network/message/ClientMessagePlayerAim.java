/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

public record ClientMessagePlayerAim(boolean isAim)
        implements IMessage<ClientMessagePlayerAim> {

    @Override
    public void encode(ClientMessagePlayerAim message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isAim);
    }

    public static ClientMessagePlayerAim decode(FriendlyByteBuf buffer) {
        return new ClientMessagePlayerAim(buffer.readBoolean());
    }

    @Override
    public void handle(ClientMessagePlayerAim message, Consumer<Runnable> handler, NetworkContext context) {
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