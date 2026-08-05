/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package dev.xcolorful.customgun.core.network;

import dev.xcolorful.customgun.core.api.network.INetworkAdapter;
import dev.xcolorful.customgun.core.api.network.MessageDirection;
import dev.xcolorful.customgun.core.api.network.message.IMessage;
import dev.xcolorful.customgun.core.network.message.*;
import dev.xcolorful.customgun.core.network.message.event.*;
import dev.xcolorful.customgun.core.network.message.handshake.Acknowledge;
import dev.xcolorful.customgun.core.network.message.handshake.ServerMessageSyncedEntityDataMapping;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class NetworkHandler {
    private static NetworkHandler INSTANCE;
    private final INetworkAdapter adapter;
    private final AtomicInteger ID_COUNT = new AtomicInteger(0);
    private final AtomicInteger HANDSHAKE_ID_COUNT = new AtomicInteger(0);
    public static final int PROTOCOL_VERSION = 105; // "1.0.5"
//    public static boolean isProtocolAccepted(String remoteVersion) {
//        return remoteVersion.equals(PROTOCOL_VERSION);
////                || remoteVersion.equals("x.x.x"); // 后续添加
//    }
//    public static Predicate<String> getProtocolAcceptancePredicate() {
//        return NetworkHandler::isProtocolAccepted;
//    }

    private NetworkHandler(INetworkAdapter adapter) {
        this.adapter = adapter;
    }

    public static void initialize(INetworkAdapter adapter) {
        INSTANCE = new NetworkHandler(adapter);
    }

    public static NetworkHandler get() {
        return INSTANCE;
    }

    public void registerMessages() {
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerShoot.class, ClientMessagePlayerShoot::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerReloadGun.class, ClientMessagePlayerReloadGun::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerCancelReload.class, ClientMessagePlayerCancelReload::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerSwitchFireMode.class, ClientMessagePlayerSwitchFireMode::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerAim.class, ClientMessagePlayerAim::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerProne.class, ClientMessagePlayerProne::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerDrawGun.class, ClientMessagePlayerDrawGun::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageSound.class, ServerMessageSound::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessageCraft.class, ClientMessageCraft::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageCraft.class, ServerMessageCraft::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerZoom.class, ClientMessagePlayerZoom::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessageRefitGun.class, ClientMessageRefitGun::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageRefreshRefitScreen.class, ServerMessageRefreshRefitScreen::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessageUnloadAttachment.class, ClientMessageUnloadAttachment::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageSwapItem.class, ServerMessageSwapItem::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerBoltGun.class, ClientMessagePlayerBoltGun::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageLevelUp.class, ServerMessageLevelUp::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunHurt.class, ServerMessageGunHurt::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunKill.class, ServerMessageGunKill::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageUpdateEntityData.class, ServerMessageUpdateEntityData::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageSyncGunPack.class, ServerMessageSyncGunPack::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessagePlayerMelee.class, ClientMessagePlayerMelee::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunDraw.class, ServerMessageGunDraw::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunFire.class, ServerMessageGunFire::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunSwitchFireMode.class, ServerMessageGunSwitchFireMode::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunMelee.class, ServerMessageGunMelee::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunReload.class, ServerMessageGunReload::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageGunShoot.class, ServerMessageGunShoot::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ServerMessageSyncBaseTimestamp.class, ServerMessageSyncBaseTimestamp::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessageSyncBaseTimestamp.class, ClientMessageSyncBaseTimestamp::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), ClientMessageLaserColor.class, ClientMessageLaserColor::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerHandshakeAcknowledge(HANDSHAKE_ID_COUNT.getAndIncrement(), Acknowledge.class, Acknowledge::decode);
        adapter.registerHandshakeMessage(HANDSHAKE_ID_COUNT.getAndIncrement(), ServerMessageSyncedEntityDataMapping.class, ServerMessageSyncedEntityDataMapping::decode);
    }

    public void sendToAllPlayers(IMessage<?> message) {
        adapter.sendToAll(message);
    }

    public void sendToPlayer(@NotNull ServerPlayer player, IMessage<?> message) {
        adapter.sendToPlayer(player, message);
    }

    /**
     * 发送给所有监听此实体的玩家
     */
    public void sendToTrackingEntityAndSelf(Entity centerEntity, IMessage<?> message) {
        adapter.sendToTrackingEntityAndSelf(centerEntity, message);
    }

    public void sendToTrackingEntity(Entity centerEntity, IMessage<?> message) {
        adapter.sendToTrackingEntity(centerEntity, message);
    }

    public void sendToServer(IMessage<?> message) {
        adapter.sendToServer(message);
    }
}
