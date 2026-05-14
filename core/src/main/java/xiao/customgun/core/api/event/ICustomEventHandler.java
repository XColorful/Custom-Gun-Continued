/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.api.event;

import xiao.customgun.CustomGun;

public interface ICustomEventHandler extends IHandler<CustomEventType, ICustomEvent> {

    @Override
    default void onReceiveWrongEvent(CustomEventType customEventType) {
        CustomGun.LOGGER.warn("{} received wrong custom event type: {}", getEventHandlerName(), customEventType);
    }
}
