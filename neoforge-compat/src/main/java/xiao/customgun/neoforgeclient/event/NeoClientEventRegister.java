/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforgeclient.event;

import xiao.customgun.CustomGun;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.core.api.event.IEventRegister;
import xiao.customgun.neoforgeclient.event.events.NeoAddClientReloadListenerEventManager;
import xiao.customgun.neoforgeclient.event.events.NeoClientPlayerTickEventManager;
import xiao.customgun.neoforgeclient.event.events.NeoClientTickEventManager;
import xiao.customgun.neoforgeclient.event.events.NeoInputKeyEventManager;
import xiao.customgun.neoforgeclient.event.events.NeoInteractionMappingEventManager;
import xiao.customgun.neoforgeclient.event.events.NeoMouseButtonEventManager;
import xiao.customgun.neoforgeclient.event.events.NeoMouseScrollingEventManager;
import xiao.customgun.neoforgeclient.event.events.NeoPrepareClientPlayerTickEventManager;
import xiao.customgun.neoforgeclient.event.events.NeoPrepareClientTickEventManager;

public class NeoClientEventRegister implements IEventRegister {

    @Override
    public boolean register(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        return switch (eventType) {
            // tick
            case PREPARE_CLIENT_TICK_EVENT -> NeoPrepareClientTickEventManager.register(eventHandler, priority, receiveCanceled);
            case CLIENT_TICK_EVENT -> NeoClientTickEventManager.register(eventHandler, priority, receiveCanceled);
            case PREPARE_CLIENT_PLAYER_TICK_EVENT -> NeoPrepareClientPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            case CLIENT_PLAYER_TICK_EVENT -> NeoClientPlayerTickEventManager.register(eventHandler, priority, receiveCanceled);
            // input
            case INPUT_KEY_EVENT -> NeoInputKeyEventManager.register(eventHandler, priority, receiveCanceled);
            case INTERACTION_MAPPING_EVENT -> NeoInteractionMappingEventManager.register(eventHandler, priority, receiveCanceled);
            case MOUSE_BUTTON_EVENT -> NeoMouseButtonEventManager.register(eventHandler, priority, receiveCanceled);
            case MOUSE_SCROLLING_EVENT -> NeoMouseScrollingEventManager.register(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_CLIENT_RELOAD_LISTENER_EVENT -> NeoAddClientReloadListenerEventManager.register(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to register handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }

    @Override
    public boolean unregister(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        return switch (eventType) {
            // tick
            case PREPARE_CLIENT_TICK_EVENT -> NeoPrepareClientTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case CLIENT_TICK_EVENT -> NeoClientTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case PREPARE_CLIENT_PLAYER_TICK_EVENT -> NeoPrepareClientPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            case CLIENT_PLAYER_TICK_EVENT -> NeoClientPlayerTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            // input
            case INPUT_KEY_EVENT -> NeoInputKeyEventManager.unregister(eventHandler, priority, receiveCanceled);
            case INTERACTION_MAPPING_EVENT -> NeoInteractionMappingEventManager.unregister(eventHandler, priority, receiveCanceled);
            case MOUSE_BUTTON_EVENT -> NeoMouseButtonEventManager.unregister(eventHandler, priority, receiveCanceled);
            case MOUSE_SCROLLING_EVENT -> NeoMouseScrollingEventManager.unregister(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_CLIENT_RELOAD_LISTENER_EVENT -> NeoAddClientReloadListenerEventManager.unregister(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to unregister handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }
}
