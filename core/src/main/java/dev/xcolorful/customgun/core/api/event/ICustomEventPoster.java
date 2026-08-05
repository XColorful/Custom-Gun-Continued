/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.event;

import dev.xcolorful.customgun.core.event.EventDispatcher;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface ICustomEventPoster {

    boolean postCustomEvent(ICustomEvent customEvent);

    @ApiStatus.Internal
    @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher(Class<? extends ICustomEvent> eventClass);
}
