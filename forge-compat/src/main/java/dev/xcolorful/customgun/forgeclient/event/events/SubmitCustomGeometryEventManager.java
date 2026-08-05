/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgeSubmitCustomGeometryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.AvailableSince("neoforge26.2")
public class SubmitCustomGeometryEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> SubmitCustomGeometryProxyHighest.INSTANCE;
            case HIGH -> SubmitCustomGeometryProxyHigh.INSTANCE;
            case NORMAL -> SubmitCustomGeometryProxyNormal.INSTANCE;
            case LOW -> SubmitCustomGeometryProxyLow.INSTANCE;
            case LOWEST -> SubmitCustomGeometryProxyLowest.INSTANCE;
        };
    }

    private static abstract class SubmitCustomGeometryProxy extends AbstractEventCommon {
        public SubmitCustomGeometryProxy() {
            super(EventType.SUBMIT_CUSTOM_GEOMETRY_EVENT);
        }

        @Override protected void registerToForge() {
            MinecraftForge.EVENT_BUS.register(this);
        }
        @Override protected void unregisterToForge() {
            MinecraftForge.EVENT_BUS.unregister(this);
        }
        @Override protected ForgeEvent getForgeEventType(Event event) {
            return new ForgeSubmitCustomGeometryEvent(event);
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