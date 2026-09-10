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

public class C2SMessageShooterReload_cancel implements IMessage<C2SMessageShooterReload_cancel> {

    public C2SMessageShooterReload_cancel() {
    }

    @Override
    public void encode(C2SMessageShooterReload_cancel message, FriendlyByteBuf buffer) {
    }

    public static C2SMessageShooterReload_cancel decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterReload_cancel();
    }

    @Override
    public void handle(C2SMessageShooterReload_cancel message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$cancelReload();
            });
        }
    }
}