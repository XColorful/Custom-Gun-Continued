/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

/*
 * 改成跟 BattleRoyale 同构的写法
 */

package xiao.customgun.core.api.event;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum CustomEventType {
    // custom
    CUSTOM_EVENT(null);

    public final Class<? extends ICustomEvent> eventClass;
    CustomEventType(Class<? extends ICustomEvent> eventClass) {
        this.eventClass = eventClass;
    }
    public @Nullable Class<? extends ICustomEvent> getEventClass() {
        return eventClass;
    }

    private static final Map<String, CustomEventType> CUSTOM_EVENT_TYPES = new HashMap<>();

    static {
        for (CustomEventType type : values()) {
            CUSTOM_EVENT_TYPES.put(type.name(), type);
        }
    }

    public static @Nullable CustomEventType fromString(String name) {
        if (name == null) return null;
        return CUSTOM_EVENT_TYPES.get(name);
    }

    public String getName() {
        return this.name();
    }
}
