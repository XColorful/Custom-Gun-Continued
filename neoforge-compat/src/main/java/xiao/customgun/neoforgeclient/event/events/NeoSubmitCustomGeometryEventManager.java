/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforgeclient.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
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

        @Override
        protected void registerToNeo() {
            NeoForge.EVENT_BUS.register(this);
        }

        @Override
        protected void unregisterToNeo() {
            NeoForge.EVENT_BUS.unregister(this);
        }

        @Override
        protected NeoEvent getNeoEventType(Event event) {
            return new NeoSubmitCustomGeometryEvent((SubmitCustomGeometryEvent) event);
        }

        protected void handle(SubmitCustomGeometryEvent event) {
            super.onEvent(event);
        }
    }

    public static class SubmitCustomGeometryProxyHighest extends SubmitCustomGeometryProxy {
        static final SubmitCustomGeometryProxyHighest INSTANCE = new SubmitCustomGeometryProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(SubmitCustomGeometryEvent e) { handle(e); }
    }

    public static class SubmitCustomGeometryProxyHigh extends SubmitCustomGeometryProxy {
        static final SubmitCustomGeometryProxyHigh INSTANCE = new SubmitCustomGeometryProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(SubmitCustomGeometryEvent e) { handle(e); }
    }

    public static class SubmitCustomGeometryProxyNormal extends SubmitCustomGeometryProxy {
        static final SubmitCustomGeometryProxyNormal INSTANCE = new SubmitCustomGeometryProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(SubmitCustomGeometryEvent e) { handle(e); }
    }

    public static class SubmitCustomGeometryProxyLow extends SubmitCustomGeometryProxy {
        static final SubmitCustomGeometryProxyLow INSTANCE = new SubmitCustomGeometryProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(SubmitCustomGeometryEvent e) { handle(e); }
    }

    public static class SubmitCustomGeometryProxyLowest extends SubmitCustomGeometryProxy {
        static final SubmitCustomGeometryProxyLowest INSTANCE = new SubmitCustomGeometryProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(SubmitCustomGeometryEvent e) { handle(e); }
    }
}
