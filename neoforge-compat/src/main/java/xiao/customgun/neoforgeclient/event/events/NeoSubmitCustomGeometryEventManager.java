/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforgeclient.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.events.AbstractNeoEventCommon;
import xiao.customgun.neoforgeclient.event.NeoSubmitCustomGeometryEvent;

@ApiStatus.AvailableSince("neoforge26.2")
public class NeoSubmitCustomGeometryEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> SubmitCustomGeometryProxyHighest.INSTANCE;
            case HIGH -> SubmitCustomGeometryProxyHigh.INSTANCE;
            case NORMAL -> SubmitCustomGeometryProxyNormal.INSTANCE;
            case LOW -> SubmitCustomGeometryProxyLow.INSTANCE;
            case LOWEST -> SubmitCustomGeometryProxyLowest.INSTANCE;
        };
    }

    private static abstract class SubmitCustomGeometryProxy extends AbstractNeoEventCommon {
        public SubmitCustomGeometryProxy() {
            super(EventType.SUBMIT_CUSTOM_GEOMETRY_EVENT);
        }

        @Override protected void registerToNeo() {
            NeoForge.EVENT_BUS.register(this);
        }
        @Override protected void unregisterToNeo() {
            NeoForge.EVENT_BUS.unregister(this);
        }
        @Override protected NeoEvent getNeoEventType(Event event) {
            return new NeoSubmitCustomGeometryEvent(event);
        }

        protected void handle(Event event) { super.onEvent(event); }
    }

    public static class SubmitCustomGeometryProxyHighest extends SubmitCustomGeometryProxy {
        static final SubmitCustomGeometryProxyHighest INSTANCE = new SubmitCustomGeometryProxyHighest();
        public void onEvent(Event e) { handle(e); }
    }

    public static class SubmitCustomGeometryProxyHigh extends SubmitCustomGeometryProxy {
        static final SubmitCustomGeometryProxyHigh INSTANCE = new SubmitCustomGeometryProxyHigh();
        public void onEvent(Event e) { handle(e); }
    }

    public static class SubmitCustomGeometryProxyNormal extends SubmitCustomGeometryProxy {
        static final SubmitCustomGeometryProxyNormal INSTANCE = new SubmitCustomGeometryProxyNormal();
        public void onEvent(Event e) { handle(e); }
    }

    public static class SubmitCustomGeometryProxyLow extends SubmitCustomGeometryProxy {
        static final SubmitCustomGeometryProxyLow INSTANCE = new SubmitCustomGeometryProxyLow();
        public void onEvent(Event e) { handle(e); }
    }

    public static class SubmitCustomGeometryProxyLowest extends SubmitCustomGeometryProxy {
        static final SubmitCustomGeometryProxyLowest INSTANCE = new SubmitCustomGeometryProxyLowest();
        public void onEvent(Event e) { handle(e); }
    }
}
