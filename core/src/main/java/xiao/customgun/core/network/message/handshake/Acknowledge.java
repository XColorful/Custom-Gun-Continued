/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.network.message.handshake;

import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.network.message.IMessage;
import xiao.customgun.core.network.LoginIndexHolder;

import java.util.function.Consumer;

public class Acknowledge extends LoginIndexHolder implements IMessage<Acknowledge> {
    public static final Marker ACKNOWLEDGE = MarkerFactory.getMarker("HANDSHAKE_ACKNOWLEDGE");

    @Override
    public void encode(Acknowledge message, FriendlyByteBuf buffer) {
    }

    public static Acknowledge decode(FriendlyByteBuf buffer) {
        return new Acknowledge();
    }

    @Override
    public void handle(Acknowledge message, Consumer<Runnable> handler, NetworkContext context) {
        CustomGun.LOGGER.debug(ACKNOWLEDGE, "Received acknowledgement from client");
        context.setHandled();
    }
}