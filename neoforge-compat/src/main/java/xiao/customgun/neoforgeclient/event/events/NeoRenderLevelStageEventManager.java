/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforgeclient.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.events.AbstractNeoEventCommon;
import xiao.customgun.neoforgeclient.event.NeoRenderLevelStageEvent;

public class NeoRenderLevelStageEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RenderLevelStageProxyHighest.INSTANCE;
            case HIGH -> RenderLevelStageProxyHigh.INSTANCE;
            case NORMAL -> RenderLevelStageProxyNormal.INSTANCE;
            case LOW -> RenderLevelStageProxyLow.INSTANCE;
            case LOWEST -> RenderLevelStageProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderLevelStageProxy extends AbstractNeoEventCommon {
        public RenderLevelStageProxy() {
            super(EventType.RENDER_LEVEL_STAGE_EVENT);
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
            return new NeoRenderLevelStageEvent((RenderLevelStageEvent) event);
        }

        protected void handle(RenderLevelStageEvent event) {
            super.onEvent(event);
        }
    }

    public static class RenderLevelStageProxyHighest extends RenderLevelStageProxy {
        static final RenderLevelStageProxyHighest INSTANCE = new RenderLevelStageProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderLevelStageEvent e) { handle(e); }
    }

    public static class RenderLevelStageProxyHigh extends RenderLevelStageProxy {
        static final RenderLevelStageProxyHigh INSTANCE = new RenderLevelStageProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderLevelStageEvent e) { handle(e); }
    }

    public static class RenderLevelStageProxyNormal extends RenderLevelStageProxy {
        static final RenderLevelStageProxyNormal INSTANCE = new RenderLevelStageProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderLevelStageEvent e) { handle(e); }
    }

    public static class RenderLevelStageProxyLow extends RenderLevelStageProxy {
        static final RenderLevelStageProxyLow INSTANCE = new RenderLevelStageProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderLevelStageEvent e) { handle(e); }
    }

    public static class RenderLevelStageProxyLowest extends RenderLevelStageProxy {
        static final RenderLevelStageProxyLowest INSTANCE = new RenderLevelStageProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderLevelStageEvent e) { handle(e); }
    }
}
