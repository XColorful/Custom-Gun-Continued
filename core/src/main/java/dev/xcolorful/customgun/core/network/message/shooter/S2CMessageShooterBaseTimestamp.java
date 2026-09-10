/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message.shooter._S2CMessageShooterBaseTimestamp;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class S2CMessageShooterBaseTimestamp implements IMessage<S2CMessageShooterBaseTimestamp> {

    public S2CMessageShooterBaseTimestamp() {
    }

    @Override
    public void encode(S2CMessageShooterBaseTimestamp message, FriendlyByteBuf buffer) {
    }

    public static S2CMessageShooterBaseTimestamp decode(FriendlyByteBuf buffer) {
        return new S2CMessageShooterBaseTimestamp();
    }

    @Override
    public void handle(S2CMessageShooterBaseTimestamp message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            long timestamp = System.currentTimeMillis();
            handler.accept(() -> {
                _S2CMessageShooterBaseTimestamp.updateBaseTimestamp(timestamp);
                context.reply(new C2SMessageShooterBaseTimestamp());
            });
        }
    }
}