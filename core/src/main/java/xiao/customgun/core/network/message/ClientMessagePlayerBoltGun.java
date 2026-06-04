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

public class ClientMessagePlayerBoltGun implements IMessage<ClientMessagePlayerBoltGun> {

    public ClientMessagePlayerBoltGun() {
    }

    @Override
    public void encode(ClientMessagePlayerBoltGun message, FriendlyByteBuf buffer) {
    }

    public static ClientMessagePlayerBoltGun decode(FriendlyByteBuf buffer) {
        return new ClientMessagePlayerBoltGun();
    }

    @Override
    public void handle(ClientMessagePlayerBoltGun message, Consumer<Runnable> handler, NetworkContext context) {
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