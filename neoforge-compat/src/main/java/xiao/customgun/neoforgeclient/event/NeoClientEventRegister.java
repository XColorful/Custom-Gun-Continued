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
import xiao.customgun.neoforgeclient.event.events.NeoClientTickEventManager;

public class NeoClientEventRegister implements IEventRegister {

    @Override
    public boolean register(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        return switch (eventType) {
            // tick
            case CLIENT_TICK_EVENT -> NeoClientTickEventManager.register(eventHandler, priority, receiveCanceled);
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
            case CLIENT_TICK_EVENT -> NeoClientTickEventManager.unregister(eventHandler, priority, receiveCanceled);
            // resource
            case ADD_CLIENT_RELOAD_LISTENER_EVENT -> NeoAddClientReloadListenerEventManager.unregister(eventHandler, priority, receiveCanceled);
            default -> {
                CustomGun.LOGGER.warn("Attempted to unregister handler for unassigned EventType: {}. Registration aborted.", eventType);
                yield false;
            }
        };
    }
}
