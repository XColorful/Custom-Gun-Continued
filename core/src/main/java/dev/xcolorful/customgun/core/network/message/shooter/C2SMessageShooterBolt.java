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

public class C2SMessageShooterBolt implements IMessage<C2SMessageShooterBolt> {

    public C2SMessageShooterBolt() {
    }

    @Override
    public void encode(C2SMessageShooterBolt message, FriendlyByteBuf buffer) {
    }

    public static C2SMessageShooterBolt decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterBolt();
    }

    @Override
    public void handle(C2SMessageShooterBolt message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$bolt();
            });
        }
    }
}