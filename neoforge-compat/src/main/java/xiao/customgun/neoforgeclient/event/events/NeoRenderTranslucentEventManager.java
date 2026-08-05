/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.client.api.event.RenderLevelStage;
import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoRenderLevelStage;
import dev.xcolorful.customgun.neoforgeclient.event.NeoRenderLevelStageEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoRenderTranslucentEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RenderTranslucentProxyHighest.INSTANCE;
            case HIGH -> RenderTranslucentProxyHigh.INSTANCE;
            case NORMAL -> RenderTranslucentProxyNormal.INSTANCE;
            case LOW -> RenderTranslucentProxyLow.INSTANCE;
            case LOWEST -> RenderTranslucentProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderTranslucentProxy extends AbstractNeoEventCommon {
        public RenderTranslucentProxy() {
            super(EventType.RENDER_TRANSLUCENT_EVENT);
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
            if (NeoRenderLevelStage.fromStage(event.getStage()) == RenderLevelStage.AFTER_TRANSLUCENT_BLOCKS) {
                super.onEvent(event);
            }
        }
    }

    public static class RenderTranslucentProxyHighest extends RenderTranslucentProxy {
        static final RenderTranslucentProxyHighest INSTANCE = new RenderTranslucentProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderLevelStageEvent e) { handle(e); }
    }

    public static class RenderTranslucentProxyHigh extends RenderTranslucentProxy {
        static final RenderTranslucentProxyHigh INSTANCE = new RenderTranslucentProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderLevelStageEvent e) { handle(e); }
    }

    public static class RenderTranslucentProxyNormal extends RenderTranslucentProxy {
        static final RenderTranslucentProxyNormal INSTANCE = new RenderTranslucentProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderLevelStageEvent e) { handle(e); }
    }

    public static class RenderTranslucentProxyLow extends RenderTranslucentProxy {
        static final RenderTranslucentProxyLow INSTANCE = new RenderTranslucentProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderLevelStageEvent e) { handle(e); }
    }

    public static class RenderTranslucentProxyLowest extends RenderTranslucentProxy {
        static final RenderTranslucentProxyLowest INSTANCE = new RenderTranslucentProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderLevelStageEvent e) { handle(e); }
    }
}
