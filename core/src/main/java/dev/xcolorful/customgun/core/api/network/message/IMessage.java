/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.network.message;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface IMessage<T> {

    void encode(T message, FriendlyByteBuf buffer);

    void handle(T message, Consumer<Runnable> handler, NetworkContext context);

    /**
     * @param sender 高版本可以获取 LocalPlayer
     * 但为了兼容性(取交集)，客户端统一从 Minecraft 获取 LocalPlayer
     * @param setHandledFunc 握手包专用，非握手包为 null
     */
    record NetworkContext(
            Connection connection,
            MessageDirection direction,
            @Nullable Player sender,
            Consumer<IMessage<?>> replyConsumer,
            @Nullable Runnable setHandledFunc
    ) {
        public void reply(IMessage<?> replyMsg) {
            replyConsumer.accept(replyMsg);
        }
        public void setHandled() {
            if (setHandledFunc != null) {
                setHandledFunc.run();
            } else {
                CustomGun.LOGGER.warn("Non-handshake message should not set handled manually");
            }
        }
    }
}
