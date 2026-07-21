/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.forge.event;

import net.minecraftforge.fml.loading.FMLLoader;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.forge.event.events.*;
import xiao.customgun.forgeclient.CustomGunForgeClient;

public class ForgeEventRegister implements IEventRegister {

    @Override
    public boolean register(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (eventType.isClientSideOnly() && FMLLoader.getDist().isClient()) {
            return _ForgeClientEventRegister.get().register(eventHandler, eventType, priority, receiveCanceled);
        }

        return switch (eventType) {
            // tick
            case PREPARE_SERVER_TICK_EVENT -> PrepareServerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case SERVER_TICK_EVENT -> ServerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case PREPARE_SERVER_PLAYER_TICK_EVENT -> PrepareServerPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case SERVER_PLAYER_TICK_EVENT -> ServerPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            // entity
            case ENTITY_JOIN_LEVEL_EVENT -> EntityJoinLevelEventManager.register(eventHandler, priority, receiveCanceled);
            // living entity
            case LIVING_KNOCKBACK_EVENT -> LivingKnockbackEventManager.register(eventHandler, priority, receiveCanceled);
            // player
            case PLAYER_CLONE_EVENT -> PlayerCloneEventManager.register(eventHandler, priority, receiveCanceled);
            case PLAYER_START_TRACKING_EVENT -> PlayerStartTrackingEventManager.register(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_SERVER_RELOAD_LISTENER_EVENT -> AddServerReloadListenerEventManager.register(eventHandler, priority, receiveCanceled);
            case TAGS_UPDATED_EVENT -> TagsUpdatedEventManager.register(eventHandler, priority, receiveCanceled);
            case DATAPACK_SYNC_EVENT -> DatapackSyncEventManager.register(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to register handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }

    @Override
    public boolean unregister(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (eventType.isClientSideOnly() && FMLLoader.getDist().isClient()) {
            return _ForgeClientEventRegister.get().unregister(eventHandler, eventType, priority, receiveCanceled);
        }

        return switch (eventType) {
            // tick
            case PREPARE_SERVER_TICK_EVENT -> PrepareServerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case SERVER_TICK_EVENT -> ServerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PREPARE_SERVER_PLAYER_TICK_EVENT -> PrepareServerPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case SERVER_PLAYER_TICK_EVENT -> ServerPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            // entity
            case ENTITY_JOIN_LEVEL_EVENT -> EntityJoinLevelEventManager.unregister(eventHandler, priority, receiveCanceled);
            // living entity
            case LIVING_KNOCKBACK_EVENT -> LivingKnockbackEventManager.unregister(eventHandler, priority, receiveCanceled);
            // player
            case PLAYER_CLONE_EVENT -> PlayerCloneEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PLAYER_START_TRACKING_EVENT -> PlayerStartTrackingEventManager.unregister(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_SERVER_RELOAD_LISTENER_EVENT -> AddServerReloadListenerEventManager.unregister(eventHandler, priority, receiveCanceled);
            case TAGS_UPDATED_EVENT -> TagsUpdatedEventManager.unregister(eventHandler, priority, receiveCanceled);
            case DATAPACK_SYNC_EVENT -> DatapackSyncEventManager.unregister(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to unregister handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }

    private static class _ForgeClientEventRegister {
        public static IEventRegister get() {
            return CustomGunForgeClient.eventRegister;
        }
    }
}

