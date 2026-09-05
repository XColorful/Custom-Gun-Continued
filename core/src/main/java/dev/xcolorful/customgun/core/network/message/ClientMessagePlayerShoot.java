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

/**
 * 射击包，表示点击了一次射击
 * @param timestamp 这里的 timestamp 应该是基于 base timestamp 的相对值
 */
public record ClientMessagePlayerShoot(long timestamp,
                                       float chargeProgress)
        implements IMessage<ClientMessagePlayerShoot> {

    @Override
    public void encode(ClientMessagePlayerShoot message, FriendlyByteBuf buffer) {
        buffer.writeLong(message.timestamp);
        buffer.writeFloat(message.chargeProgress);
    }

    public static ClientMessagePlayerShoot decode(FriendlyByteBuf buffer) {
        return new ClientMessagePlayerShoot(buffer.readLong(), buffer.readFloat());
    }

    @Override
    public void handle(ClientMessagePlayerShoot message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                ILivingShooterGetter.cgc$fromLivingEntity(player).cgc$shoot(player::getXRot, player::getYRot, message.timestamp, message.chargeProgress);
            });
        }
    }
}