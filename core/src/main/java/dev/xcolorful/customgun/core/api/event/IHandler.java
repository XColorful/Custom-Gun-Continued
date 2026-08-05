/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.event;

import dev.xcolorful.customgun.CustomGun;

/**
 * @param <Y> 事件类型枚举 (EventType 或 CustomEventType)
 * @param <E> 事件实例类型 (IEvent 或 ICustomEvent)
 */
public interface IHandler<Y, E extends IEvent> {
    String getEventHandlerName();

    void handleEvent(Y type, E event);

    default void onReceiveWrongEvent(Y eventType) {
        CustomGun.LOGGER.warn("{} received wrong event type: {}", getEventHandlerName(), eventType);
    }
}