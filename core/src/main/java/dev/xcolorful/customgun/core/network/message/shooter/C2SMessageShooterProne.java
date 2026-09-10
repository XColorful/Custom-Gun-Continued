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
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record C2SMessageShooterProne(boolean isProne)
        implements IMessage<C2SMessageShooterProne> {

    @Override
    public void encode(C2SMessageShooterProne message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.isProne);
    }

    public static C2SMessageShooterProne decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterProne(buffer.readBoolean());
    }

    @Override
    public void handle(C2SMessageShooterProne message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                @Nullable LivingEntity livingShooter = context.sender();
                if (livingShooter == null) return;

                ILivingShooterGetter.cgc$fromLivingEntity(livingShooter).cgc$prone(message.isProne);
            });
        }
    }
}