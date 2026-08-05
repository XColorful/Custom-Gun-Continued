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

public class ClientMessagePlayerCancelReload implements IMessage<ClientMessagePlayerCancelReload> {

    public ClientMessagePlayerCancelReload() {
    }

    @Override
    public void encode(ClientMessagePlayerCancelReload message, FriendlyByteBuf buffer) {
    }

    public static ClientMessagePlayerCancelReload decode(FriendlyByteBuf buffer) {
        return new ClientMessagePlayerCancelReload();
    }

    @Override
    public void handle(ClientMessagePlayerCancelReload message, Consumer<Runnable> handler, NetworkContext context) {
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