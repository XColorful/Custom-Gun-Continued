/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity;

import dev.xcolorful.customgun.core.api.event.*;
import dev.xcolorful.customgun.core.entity.sync.DataEntry;
import dev.xcolorful.customgun.core.entity.sync.SyncDataHolder;
import dev.xcolorful.customgun.core.entity.sync.SyncedDataKey;
import dev.xcolorful.customgun.core.entity.sync.SyncedEntityData;
import dev.xcolorful.customgun.core.network.message.ServerMessageSyncBaseTimestamp;
import dev.xcolorful.customgun.core.network.message.ServerMessageUpdateEntityData;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LivingShooterSyncHandler implements IEventHandler {
    private static class SyncEntityDataHandlerHolder {
        private static final LivingShooterSyncHandler INSTANCE = new LivingShooterSyncHandler();
    }
    public static LivingShooterSyncHandler get() {
        return SyncEntityDataHandlerHolder.INSTANCE;
    }
    protected LivingShooterSyncHandler() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        switch (eventType) {
            case SERVER_TICK_EVENT -> onServerTick((IServerTickEvent) event);
            case ENTITY_JOIN_LEVEL_EVENT -> onPlayerJoinWorld((IEntityJoinLevelEvent) event);
            case PLAYER_CLONE_EVENT -> onPlayerClone((IPlayerCloneEvent) event);
            case PLAYER_START_TRACKING_EVENT -> onPlayerStartTracking((IPlayerStartTrackingEvent) event);
        }
    }

    private void onServerTick(IServerTickEvent event) {
        SyncedEntityData instance = SyncedEntityData.instance();
        if (!instance.isDirty()) {
            return;
        }

        List<Entity> dirtyEntities = instance.getDirtyEntities();
        if (dirtyEntities.isEmpty()) {
            instance.setDirty(false);
            return;
        }
        for (Entity entity : dirtyEntities) {
            SyncDataHolder holder = instance.getSyncDataHolder(entity);
            if (holder == null || !holder.isDirty()) {
                continue;
            }
            List<DataEntry<?, ?>> entries = holder.gatherDirty();
            if (entries.isEmpty()) {
                continue;
            }
            List<DataEntry<?, ?>> selfEntries = entries.stream().filter(entry -> entry.getKey().syncMode().isSelf()).collect(Collectors.toList());
            if (!selfEntries.isEmpty() && entity instanceof ServerPlayer serverPlayer) {
                SendUtils.sendMessageToPlayer(serverPlayer, new ServerMessageUpdateEntityData(entity.getId(), selfEntries));
            }
            List<DataEntry<?, ?>> trackingEntries = entries.stream().filter(entry -> entry.getKey().syncMode().isTracking()).collect(Collectors.toList());
            if (!trackingEntries.isEmpty()) {
                SendUtils.sendMessageToTrackingEntity(entity, new ServerMessageUpdateEntityData(entity.getId(), trackingEntries));
            }
            holder.clean();
        }
        dirtyEntities.clear();
        instance.setDirty(false);
    }

    private void onPlayerClone(IPlayerCloneEvent event) {
        Player original = event.getOriginalPlayer();
        original.reviveCaps();
        SyncDataHolder oldHolder = SyncedEntityData.instance().getSyncDataHolder(original);
        if (oldHolder == null) {
            return;
        }
        original.invalidateCaps();
        Player player = event.getEntity();
        SyncDataHolder newHolder = SyncedEntityData.instance().getSyncDataHolder(player);
        if (newHolder == null) {
            return;
        }
        Map<SyncedDataKey<?, ?>, DataEntry<?, ?>> dataMap = new HashMap<>(oldHolder.syncData);
        if (event.isCausedByDeath()) {
            dataMap.entrySet().removeIf(entry -> !entry.getKey().persistent());
        }
        newHolder.syncData = dataMap;
    }

    private void onPlayerJoinWorld(IEntityJoinLevelEvent event) {
//        if (!event.getLogicalSide().isServer()) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer serverPlayer)) {
            return;
        }

        SendUtils.sendMessageToPlayer(serverPlayer, new ServerMessageSyncBaseTimestamp());

        SyncDataHolder holder = SyncedEntityData.instance().getSyncDataHolder(serverPlayer);
        if (holder != null) {
            List<DataEntry<?, ?>> entries = holder.gatherAll();
            if (!entries.isEmpty()) {
                SendUtils.sendMessageToPlayer(serverPlayer, new ServerMessageUpdateEntityData(entity.getId(), entries));
            }
        }
    }

    private void onPlayerStartTracking(IPlayerStartTrackingEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Entity entity = event.getTarget();
        SyncDataHolder holder = SyncedEntityData.instance().getSyncDataHolder(entity);
        if (holder != null) {
            List<DataEntry<?, ?>> entries = holder.gatherAll();
            entries.removeIf(entry -> !entry.getKey().syncMode().isTracking());
            if (!entries.isEmpty()) {
                SendUtils.sendMessageToPlayer(serverPlayer, new ServerMessageUpdateEntityData(entity.getId(), entries));
            }
        }
    }
}
