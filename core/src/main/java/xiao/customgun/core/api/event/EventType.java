/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.api.event;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public enum EventType {
    // tick
    SERVER_TICK_EVENT(false),
    CLIENT_TICK_EVENT(true),
    // entity
    ENTITY_JOIN_LEVEL_EVENT(false),
    // player
    PLAYER_CLONE_EVENT(false),
    PLAYER_START_TRACKING_EVENT(false),
    // resource
    ADD_SERVER_RELOAD_LISTENER_EVENT(false),
    ADD_CLIENT_RELOAD_LISTENER_EVENT(true),
    TAGS_UPDATED_EVENT(false),
    DATAPACK_SYNC_EVENT(false);

    public final boolean isClientSideOnly;
    EventType(boolean isClientSideOnly) {
        this.isClientSideOnly = isClientSideOnly;
    }

    private static final Map<String, EventType> EVENT_TYPES = new HashMap<>();

    static {
        for (EventType type : values()) {
            EVENT_TYPES.put(type.name(), type);
        }
    }

    public static @Nullable EventType fromString(String name) {
        if (name == null) return null;
        return EVENT_TYPES.get(name);
    }

    public String getName() {
        return this.name();
    }

    public boolean isClientSideOnly() {
        return this.isClientSideOnly;
    }
    public boolean isUnsafeOnDedicatedServer() {
        return isClientSideOnly();
    }
}
