/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message.handshake;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.network.LoginIndexHolder;
import net.minecraft.network.FriendlyByteBuf;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.function.Consumer;

public class C2SMessageAcknowledge extends LoginIndexHolder implements IMessage<C2SMessageAcknowledge> {
    public static final Marker ACKNOWLEDGE = MarkerFactory.getMarker("HANDSHAKE_ACKNOWLEDGE");

    public C2SMessageAcknowledge() {
    }

    @Override
    public void encode(C2SMessageAcknowledge message, FriendlyByteBuf buffer) {
    }

    public static C2SMessageAcknowledge decode(FriendlyByteBuf buffer) {
        return new C2SMessageAcknowledge();
    }

    @Override
    public void handle(C2SMessageAcknowledge message, Consumer<Runnable> handler, NetworkContext context) {
        CustomGun.LOGGER.debug(ACKNOWLEDGE, "Received acknowledgement from client");
        context.setHandled();
    }
}