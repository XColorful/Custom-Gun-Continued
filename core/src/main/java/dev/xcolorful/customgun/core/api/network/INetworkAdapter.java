/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.network;

import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.network.LoginIndexHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public interface INetworkAdapter {

    /**
     * 注册一个消息类型。
     * 必须在 Mod 加载阶段（CommonSetup 或更早）调用。
     * @param id 消息ID。
     * @param clazz 消息的类。
     * @param direction 消息的方向（客户端->服务器 或 服务器->客户端）。
     */
    <T extends IMessage<T>> void registerMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder, MessageDirection direction);

    void sendToAll(IMessage<?> message);

    void sendToPlayer(ServerPlayer player, IMessage<?> message);

    // 新增接口
    <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeAcknowledge(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder);
    <T extends LoginIndexHolder & IMessage<T>> void registerHandshakeMessage(int id, Class<T> clazz, Function<FriendlyByteBuf, T> decoder);
    void sendToTrackingEntityAndSelf(Entity centerEntity, IMessage<?> message);
    void sendToTrackingEntity(Entity centerEntity, IMessage<?> message);
    void sendToServer(IMessage<?> message);
}
