/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.client.network.message._ServerMessageRefreshRefitScreen;
import dev.xcolorful.customgun.core.api.common.McSide;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Consumer;

public class ServerMessageRefreshRefitScreen implements IMessage<ServerMessageRefreshRefitScreen> {

    public ServerMessageRefreshRefitScreen() {
    }

    @Override
    public void encode(ServerMessageRefreshRefitScreen message, FriendlyByteBuf buffer) {
    }

    public static ServerMessageRefreshRefitScreen decode(FriendlyByteBuf buffer) {
        return new ServerMessageRefreshRefitScreen();
    }

    @Override
    public void handle(ServerMessageRefreshRefitScreen message, Consumer<Runnable> handler, NetworkContext context) {
        if (CustomGun.getSideExecutor().getLogicalSide().isClient()) {
            handler.accept(() -> CustomGun.getSideExecutor().executeOn(McSide.CLIENT, () -> _ServerMessageRefreshRefitScreen::updateScreen
            ));
        }
    }
}