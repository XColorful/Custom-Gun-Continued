/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.event;

public interface IEventRegister {

    default boolean register(IEventHandler eventHandler, EventType eventType) {
        return register(eventHandler, eventType, EventPriority.NORMAL, false);
    }
    boolean register(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled);

    default boolean unregister(IEventHandler eventHandler, EventType eventType) {
        return unregister(eventHandler, eventType, EventPriority.NORMAL, false);
    }
    boolean unregister(IEventHandler eventHandler, EventType eventType, EventPriority priority, boolean receiveCanceled);
}