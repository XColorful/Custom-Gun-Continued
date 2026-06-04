/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.event;

import xiao.customgun.CustomGun;

public class EventRegister implements ICustomEventRegister {

    private static class EventRegisterHolder {
        private static final EventRegister INSTANCE = new EventRegister();
    }

    public static ICustomEventRegister get() {
        return EventRegisterHolder.INSTANCE;
    }

    protected EventRegister() {}

    private static IEventRegister eventRegister;

    public static void initialize(IEventRegister eventRegister) {
        if (EventRegister.eventRegister == null) {
            EventRegister.eventRegister = eventRegister;
        }
    }

    // -------- Forge & NeoForge事件 --------
    public boolean register(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (eventRegister == null) {
            throw new IllegalStateException("Event register has not been initialized. Call init() first.");
        }
        return eventRegister.register(eventHandler, eventType, priority, receiveCanceled);
    }
    public boolean unregister(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled) {
        if (eventRegister == null) {
            throw new IllegalStateException("Event register has not been initialized. Call init() first.");
        }
        return eventRegister.unregister(eventHandler, eventType, priority, receiveCanceled);
    }

    // -------- 自定义事件 --------

    @Override
    public boolean register(ICustomEventHandler eventHandler, CustomEventType customEventType, EventPriority priority, boolean receiveCanceled) {
        Class<? extends ICustomEvent> eventClass = customEventType.getEventClass();
        if (eventClass == null) {
            CustomGun.LOGGER.warn("Attempted to register handler for unassigned CustomEventType: {}. Registration aborted.", customEventType);
            return false;
        }
        
        return register(eventHandler, eventClass, priority, receiveCanceled);
    }
    @Override
    public <T extends ICustomEvent> boolean register(ICustomEventHandler eventHandler, Class<T> eventClass, EventPriority priority, boolean receiveCanceled) {
        return CustomGun.getEventPoster().getEventDispatcher(eventClass)
                .registerHandler(eventHandler, priority, receiveCanceled);
    }

    @Override
    public boolean unregister(ICustomEventHandler eventHandler, CustomEventType customEventType, EventPriority priority, boolean receiveCanceled) {
        Class<? extends ICustomEvent> eventClass = customEventType.getEventClass();
        if (eventClass == null) {
            CustomGun.LOGGER.warn("Attempted to unregister handler for unassigned CustomEventType: {}. Unregistration aborted.", customEventType);
            return false;
        }

        return unregister(eventHandler, eventClass, priority, receiveCanceled);
    }
    @Override
    public <T extends ICustomEvent> boolean unregister(ICustomEventHandler eventHandler, Class<T> eventClass, EventPriority priority, boolean receiveCanceled) {
        return CustomGun.getEventPoster().getEventDispatcher(eventClass)
                .unregisterHandler(eventHandler, priority, receiveCanceled);
    }
}