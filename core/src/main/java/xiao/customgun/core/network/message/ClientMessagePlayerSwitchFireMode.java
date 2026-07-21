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

import java.util.function.Consumer;

public class ClientMessagePlayerSwitchFireMode implements IMessage<ClientMessagePlayerSwitchFireMode> {

    public ClientMessagePlayerSwitchFireMode() {
    }

    @Override
    public void encode(ClientMessagePlayerSwitchFireMode message, FriendlyByteBuf buffer) {
    }

    public static ClientMessagePlayerSwitchFireMode decode(FriendlyByteBuf buffer) {
        return new ClientMessagePlayerSwitchFireMode();
    }

    @Override
    public void handle(ClientMessagePlayerSwitchFireMode message, Consumer<Runnable> handler, NetworkContext context) {
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