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

public class C2SMessageShooterSwitchFireMode implements IMessage<C2SMessageShooterSwitchFireMode> {

    public C2SMessageShooterSwitchFireMode() {
    }

    @Override
    public void encode(C2SMessageShooterSwitchFireMode message, FriendlyByteBuf buffer) {
    }

    public static C2SMessageShooterSwitchFireMode decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterSwitchFireMode();
    }

    @Override
    public void handle(C2SMessageShooterSwitchFireMode message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$switchFireMode();
            });
        }
    }
}