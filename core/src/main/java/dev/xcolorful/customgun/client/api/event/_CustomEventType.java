/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.client.api.event.player.SwapItemWithOffHandEvent;
import dev.xcolorful.customgun.client.api.event.render.BeforeRenderHandEvent;
import dev.xcolorful.customgun.client.api.event.render.ItemInHandBobEvent;
import dev.xcolorful.customgun.client.api.event.render.LevelBobEvent;
import dev.xcolorful.customgun.core.api.event.CustomEventType;
import dev.xcolorful.customgun.core.api.event.ICustomEvent;

/**
 * 该类用于防止客户端类被意外调用，CustomEventType默认不会触发类初始化
 */
public enum _CustomEventType {
    // player
    SWAP_ITEM_WITH_OFFHAND_EVENT(SwapItemWithOffHandEvent.class,
            CustomEventType.SWAP_ITEM_WITH_OFFHAND_EVENT),
    // render
    BEFORE_RENDER_HAND_EVENT(BeforeRenderHandEvent.class,
            CustomEventType.BEFORE_RENDER_HAND_EVENT),
    ITEM_IN_HAND_BOB_HURT_EVENT(ItemInHandBobEvent.Hurt.class,
            CustomEventType.ITEM_IN_HAND_BOB_HURT_EVENT),
    ITEM_IN_HAND_BOB_VIEW_EVENT(ItemInHandBobEvent.View.class,
            CustomEventType.ITEM_IN_HAND_BOB_VIEW_EVENT),
    LEVEL_BOB_HURT_EVENT(LevelBobEvent.Hurt.class,
            CustomEventType.LEVEL_BOB_HURT_EVENT),
    LEVEL_BOB_VIEW_EVENT(LevelBobEvent.View.class,
            CustomEventType.LEVEL_BOB_VIEW_EVENT);

    public final Class<? extends ICustomEvent> eventClass;
    public final CustomEventType customEventType;
    _CustomEventType(Class<? extends ICustomEvent> eventClass, CustomEventType customEventType) {
        this.eventClass = eventClass;
        this.customEventType = customEventType;
    }

    private static boolean initialized;
    public static void mixinClientEventClass() {
        if (initialized) return;

        for (var type : values()) {
            type.customEventType.setEventClass(type.eventClass);
        }

        initialized = true;
    }
}
