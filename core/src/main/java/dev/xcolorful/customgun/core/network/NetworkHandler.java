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
import dev.xcolorful.customgun.core.network.message.gun.S2CMessageGunFire;
import dev.xcolorful.customgun.core.network.message.gun.S2CMessageGunLevelUp;
import dev.xcolorful.customgun.core.network.message.handshake.C2SMessageAcknowledge;
import dev.xcolorful.customgun.core.network.message.handshake.S2CMessageSyncedEntityDataMapping;
import dev.xcolorful.customgun.core.network.message.player.C2SMessagePlayerCraft;
import dev.xcolorful.customgun.core.network.message.player.S2CMessagePlayerCraft;
import dev.xcolorful.customgun.core.network.message.player.S2CMessagePlayerSwapItem;
import dev.xcolorful.customgun.core.network.message.projectile.S2CMessageProjectileHit;
import dev.xcolorful.customgun.core.network.message.projectile.S2CMessageProjectileKill;
import dev.xcolorful.customgun.core.network.message.resource.S2CMessageResourceSound;
import dev.xcolorful.customgun.core.network.message.resource.S2CMessageSyncDataPack;
import dev.xcolorful.customgun.core.network.message.shooter.*;
import dev.xcolorful.customgun.core.network.message.sync.S2CMessageUpdateEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class NetworkHandler {
    private static NetworkHandler INSTANCE;

    private final INetworkAdapter adapter;
    private final AtomicInteger ID_COUNT = new AtomicInteger(0);
    private final AtomicInteger HANDSHAKE_ID_COUNT = new AtomicInteger(0);

    public static final int protocol_version = 12;
    public static final String PROTOCOL_VERSION = String.valueOf(protocol_version);
    public static boolean isProtocolAccepted(String remoteVersion) {
        return remoteVersion.equals(PROTOCOL_VERSION);
    }
    public static Predicate<String> getProtocolAcceptancePredicate() {
        return NetworkHandler::isProtocolAccepted;
    }

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
        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterShoot.class, C2SMessageShooterShoot::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterReload.class, C2SMessageShooterReload::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterReload_cancel.class, C2SMessageShooterReload_cancel::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterSwitchFireMode.class, C2SMessageShooterSwitchFireMode::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterAim.class, C2SMessageShooterAim::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterProne.class, C2SMessageShooterProne::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterDraw.class, C2SMessageShooterDraw::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageResourceSound.class, S2CMessageResourceSound::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessagePlayerCraft.class, C2SMessagePlayerCraft::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessagePlayerCraft.class, S2CMessagePlayerCraft::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterZoom.class, C2SMessageShooterZoom::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterRefit_install.class, C2SMessageShooterRefit_install::decode, MessageDirection.CLIENT_TO_SERVER);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageShooterRefit_refresh.class, S2CMessageShooterRefit_refresh::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterRefit_unload.class, C2SMessageShooterRefit_unload::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessagePlayerSwapItem.class, S2CMessagePlayerSwapItem::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterBolt.class, C2SMessageShooterBolt::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageGunLevelUp.class, S2CMessageGunLevelUp::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageProjectileHit.class, S2CMessageProjectileHit::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageProjectileKill.class, S2CMessageProjectileKill::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageUpdateEntityData.class, S2CMessageUpdateEntityData::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageSyncDataPack.class, S2CMessageSyncDataPack::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterMelee.class, C2SMessageShooterMelee::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageShooterDraw.class, S2CMessageShooterDraw::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageGunFire.class, S2CMessageGunFire::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageShooterSwitchFireMode.class, S2CMessageShooterSwitchFireMode::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageShooterMelee.class, S2CMessageShooterMelee::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageShooterReload.class, S2CMessageShooterReload::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageShooterReloadFeed.class, S2CMessageShooterReloadFeed::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageShooterFire.class, S2CMessageShooterFire::decode, MessageDirection.SERVER_TO_CLIENT);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), S2CMessageShooterBaseTimestamp.class, S2CMessageShooterBaseTimestamp::decode, MessageDirection.SERVER_TO_CLIENT);
        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterBaseTimestamp.class, C2SMessageShooterBaseTimestamp::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerMessage(ID_COUNT.getAndIncrement(), C2SMessageShooterRefit_laserColor.class, C2SMessageShooterRefit_laserColor::decode, MessageDirection.CLIENT_TO_SERVER);

        adapter.registerHandshakeAcknowledge(HANDSHAKE_ID_COUNT.getAndIncrement(), C2SMessageAcknowledge.class, C2SMessageAcknowledge::decode);
        adapter.registerHandshakeMessage(HANDSHAKE_ID_COUNT.getAndIncrement(), S2CMessageSyncedEntityDataMapping.class, S2CMessageSyncedEntityDataMapping::decode);
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
