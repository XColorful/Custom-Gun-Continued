/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.config.SyncConfig;

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
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                if (!SyncConfig.ENABLE_PRONE.get()) {
                    return;
                }

                ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$prone(message.isProne);
            });
        }
    }
}