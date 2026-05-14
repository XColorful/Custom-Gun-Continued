/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforge.event;

import net.neoforged.fml.loading.FMLLoader;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.neoforge.event.events.NeoAddServerReloadListenerEventManager;
import xiao.customgun.neoforge.event.events.NeoDatapackSyncEventManager;
import xiao.customgun.neoforge.event.events.NeoServerTickEventManager;
import xiao.customgun.neoforge.event.events.NeoTagsUpdatedEventManager;
import xiao.customgun.neoforgeclient.CustomGunNeoforgeClient;

public class NeoEventRegister implements IEventRegister {

    @Override
    public boolean register(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (eventType.isClientSideOnly() && FMLLoader.getDist().isClient()) {
            return _NeoClientEventRegister.get().register(eventHandler, eventType, priority, receiveCanceled);
        }

        return switch (eventType) {
            case SERVER_TICK_EVENT -> NeoServerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case ADD_SERVER_RELOAD_LISTENER_EVENT -> NeoAddServerReloadListenerEventManager.register(eventHandler, priority, receiveCanceled);
            case TAGS_UPDATED_EVENT -> NeoTagsUpdatedEventManager.register(eventHandler, priority, receiveCanceled);
            case DATAPACK_SYNC_EVENT -> NeoDatapackSyncEventManager.register(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to register handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }

    @Override
    public boolean unregister(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (eventType.isClientSideOnly() && FMLLoader.getDist().isClient()) {
            return _NeoClientEventRegister.get().unregister(eventHandler, eventType, priority, receiveCanceled);
        }

        return switch (eventType) {
            case SERVER_TICK_EVENT -> NeoServerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case ADD_SERVER_RELOAD_LISTENER_EVENT -> NeoAddServerReloadListenerEventManager.unregister(eventHandler, priority, receiveCanceled);
            case TAGS_UPDATED_EVENT -> NeoTagsUpdatedEventManager.unregister(eventHandler, priority, receiveCanceled);
            case DATAPACK_SYNC_EVENT -> NeoDatapackSyncEventManager.unregister(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to unregister handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }

    private static class _NeoClientEventRegister {
        public static IEventRegister get() {
            return CustomGunNeoforgeClient.eventRegister;
        }
    }
}
