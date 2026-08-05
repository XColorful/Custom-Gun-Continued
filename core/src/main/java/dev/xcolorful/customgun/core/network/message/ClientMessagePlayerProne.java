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
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record ClientMessagePlayerProne(boolean isProne)
        implements IMessage<ClientMessagePlayerProne> {

    @Override
    public void encode(ClientMessagePlayerProne message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isProne);
    }

    public static ClientMessagePlayerProne decode(FriendlyByteBuf buffer) {
        return new ClientMessagePlayerProne(buffer.readBoolean());
    }

    @Override
    public void handle(ClientMessagePlayerProne message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                @Nullable LivingEntity livingShooter = context.sender();
                if (livingShooter == null) return;

                ILivingShooterGetter.cgc$fromLivingEntity(livingShooter).cgc$prone(message.isProne);
            });
        }
    }
}