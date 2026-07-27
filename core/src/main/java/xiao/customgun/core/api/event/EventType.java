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
    PREPARE_SERVER_TICK_EVENT(false),
    SERVER_TICK_EVENT(false),
    PREPARE_CLIENT_TICK_EVENT(true),
    CLIENT_TICK_EVENT(true),
    PREPARE_SERVER_PLAYER_TICK_EVENT(false),
    SERVER_PLAYER_TICK_EVENT(false),
    PREPARE_CLIENT_PLAYER_TICK_EVENT(true),
    CLIENT_PLAYER_TICK_EVENT(true),
    // entity
    ENTITY_JOIN_LEVEL_EVENT(false),
    // living entity
    LIVING_ATTACK_EVENT(false),
    LIVING_HURT_EVENT(false),
    LIVING_DAMAGE_EVENT(false),
    LIVING_DEATH_EVENT(false),
    LIVING_HEAL_EVENT(false),
    LIVING_USE_TOTEM_EVENT(false),
    LIVING_KNOCKBACK_EVENT(false),
    // player
    PLAYER_CLONE_EVENT(false),
    PLAYER_START_TRACKING_EVENT(false),
    // input
    INPUT_KEY_EVENT(true),
    INTERACTION_MAPPING_EVENT(true),
    MOUSE_BUTTON_EVENT(true),
    MOUSE_SCROLLING_EVENT(true),
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
