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
import dev.xcolorful.customgun.neoforge.event.NeoLivingDamageEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class NeoLivingDamageEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> LivingDamageProxyHighest.INSTANCE;
            case HIGH -> LivingDamageProxyHigh.INSTANCE;
            case NORMAL -> LivingDamageProxyNormal.INSTANCE;
            case LOW -> LivingDamageProxyLow.INSTANCE;
            case LOWEST -> LivingDamageProxyLowest.INSTANCE;
        };
    }

    private static abstract class LivingDamageProxy extends AbstractNeoEventCommon {
        public LivingDamageProxy() {
            super(EventType.LIVING_DAMAGE_EVENT);
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
            return new NeoLivingDamageEvent(event);
        }

        protected void handle(LivingDamageEvent event) {
            super.onEvent(event);
        }
    }

    public static class LivingDamageProxyHighest extends LivingDamageProxy {
        static final LivingDamageProxyHighest INSTANCE = new LivingDamageProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(LivingDamageEvent e) { handle(e); }
    }

    public static class LivingDamageProxyHigh extends LivingDamageProxy {
        static final LivingDamageProxyHigh INSTANCE = new LivingDamageProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(LivingDamageEvent e) { handle(e); }
    }

    public static class LivingDamageProxyNormal extends LivingDamageProxy {
        static final LivingDamageProxyNormal INSTANCE = new LivingDamageProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(LivingDamageEvent e) { handle(e); }
    }

    public static class LivingDamageProxyLow extends LivingDamageProxy {
        static final LivingDamageProxyLow INSTANCE = new LivingDamageProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(LivingDamageEvent e) { handle(e); }
    }

    public static class LivingDamageProxyLowest extends LivingDamageProxy {
        static final LivingDamageProxyLowest INSTANCE = new LivingDamageProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(LivingDamageEvent e) { handle(e); }
    }
}
