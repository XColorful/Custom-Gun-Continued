/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShootResult;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Consumer;

/**
 * 射击包，表示点击了一次射击
 * @param timestamp 这里的 timestamp 应该是基于 base timestamp 的相对值
 */
public record C2SMessageShooterShoot(long timestamp,
                                     float chargeProgress)
        implements IMessage<C2SMessageShooterShoot> {

    @Override
    public void encode(C2SMessageShooterShoot message, FriendlyByteBuf buffer) {
        buffer.writeLong(message.timestamp);
        buffer.writeFloat(message.chargeProgress);
    }

    public static C2SMessageShooterShoot decode(FriendlyByteBuf buffer) {
        return new C2SMessageShooterShoot(buffer.readLong(), buffer.readFloat());
    }

    @Override
    public void handle(C2SMessageShooterShoot message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isServer()) {
            handler.accept(() -> {
                if (!(context.sender() instanceof ServerPlayer player)) {
                    return;
                }

                ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(player);
                ShootResult shootResult = iLivingShooter.cgc$shoot(player::getXRot, player::getYRot, message.timestamp, message.chargeProgress);
                if (shootResult != ShootResult.SUCCESS) {
                    CustomGun.LOGGER.debug("C2SMessageShooterShoot: {} shoot failed ({})", player.getName().getString(), shootResult);
                }
            });
        }
    }
}