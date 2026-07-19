/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.event.render;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.event.CustomEventType;
import xiao.customgun.core.api.event.ICustomEvent;
import xiao.customgun.core.api.event.ICustomEventHandler;
import xiao.customgun.core.event.EventDispatcher;

public abstract class LevelBobEvent extends RenderBobEvent {

    public LevelBobEvent() {
    }

    public static class Hurt extends LevelBobEvent {

        public Hurt() {
        }
        @Override public CustomEventType getEventType() {
            return CustomEventType.LEVEL_BOB_HURT_EVENT;
        }

        @Override public String getTextName() {
            return "LevelBobEvent.Hurt";
        }
        @Override public Component getDisplayName() {
            return Component.literal(getTextName());
        }

        private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(LevelBobEvent.Hurt.class);
        @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
            return _EVENT_DISPATCHER;
        }
    }

    public static class View extends LevelBobEvent {

        public View() {
        }
        @Override public CustomEventType getEventType() {
            return CustomEventType.LEVEL_BOB_VIEW_EVENT;
        }

        @Override public String getTextName() {
            return "LevelBobEvent.View";
        }
        @Override public Component getDisplayName() {
            return Component.literal(getTextName());
        }

        private static final EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> _EVENT_DISPATCHER = CustomGun.getEventPoster().getEventDispatcher(LevelBobEvent.View.class);
        @Override public @NotNull EventDispatcher<ICustomEventHandler, ICustomEvent, CustomEventType> getEventDispatcher() {
            return _EVENT_DISPATCHER;
        }
    }
}
