/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforgeclient.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.events.AbstractNeoEventCommon;
import xiao.customgun.neoforgeclient.event.NeoRenderGuiEvent;

public class NeoRenderGuiEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RenderGuiProxyHighest.INSTANCE;
            case HIGH -> RenderGuiProxyHigh.INSTANCE;
            case NORMAL -> RenderGuiProxyNormal.INSTANCE;
            case LOW -> RenderGuiProxyLow.INSTANCE;
            case LOWEST -> RenderGuiProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderGuiProxy extends AbstractNeoEventCommon {
        public RenderGuiProxy() {
            super(EventType.RENDER_GUI_EVENT);
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
            return new NeoRenderGuiEvent(event);
        }

        protected void handle(RenderGuiEvent.Post event) {
            super.onEvent(event);
        }
    }

    public static class RenderGuiProxyHighest extends RenderGuiProxy {
        static final RenderGuiProxyHighest INSTANCE = new RenderGuiProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderGuiProxyHigh extends RenderGuiProxy {
        static final RenderGuiProxyHigh INSTANCE = new RenderGuiProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderGuiProxyNormal extends RenderGuiProxy {
        static final RenderGuiProxyNormal INSTANCE = new RenderGuiProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderGuiProxyLow extends RenderGuiProxy {
        static final RenderGuiProxyLow INSTANCE = new RenderGuiProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderGuiProxyLowest extends RenderGuiProxy {
        static final RenderGuiProxyLowest INSTANCE = new RenderGuiProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }
}
