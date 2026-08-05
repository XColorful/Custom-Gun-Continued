/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.NeoLivingDeathEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class NeoLivingDeathEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> LivingDeathProxyHighest.INSTANCE;
            case HIGH -> LivingDeathProxyHigh.INSTANCE;
            case NORMAL -> LivingDeathProxyNormal.INSTANCE;
            case LOW -> LivingDeathProxyLow.INSTANCE;
            case LOWEST -> LivingDeathProxyLowest.INSTANCE;
        };
    }

    private static abstract class LivingDeathProxy extends AbstractNeoEventCommon {
        public LivingDeathProxy() {
            super(EventType.LIVING_DEATH_EVENT);
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
            return new NeoLivingDeathEvent(event);
        }

        protected void handle(LivingDeathEvent event) {
            super.onEvent(event);
        }
    }

    public static class LivingDeathProxyHighest extends LivingDeathProxy {
        static final LivingDeathProxyHighest INSTANCE = new LivingDeathProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(LivingDeathEvent e) { handle(e); }
    }

    public static class LivingDeathProxyHigh extends LivingDeathProxy {
        static final LivingDeathProxyHigh INSTANCE = new LivingDeathProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(LivingDeathEvent e) { handle(e); }
    }

    public static class LivingDeathProxyNormal extends LivingDeathProxy {
        static final LivingDeathProxyNormal INSTANCE = new LivingDeathProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(LivingDeathEvent e) { handle(e); }
    }

    public static class LivingDeathProxyLow extends LivingDeathProxy {
        static final LivingDeathProxyLow INSTANCE = new LivingDeathProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(LivingDeathEvent e) { handle(e); }
    }

    public static class LivingDeathProxyLowest extends LivingDeathProxy {
        static final LivingDeathProxyLowest INSTANCE = new LivingDeathProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(LivingDeathEvent e) { handle(e); }
    }
}
