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
import dev.xcolorful.customgun.neoforge.event.NeoLivingHealEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

public class NeoLivingHealEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> LivingHealProxyHighest.INSTANCE;
            case HIGH -> LivingHealProxyHigh.INSTANCE;
            case NORMAL -> LivingHealProxyNormal.INSTANCE;
            case LOW -> LivingHealProxyLow.INSTANCE;
            case LOWEST -> LivingHealProxyLowest.INSTANCE;
        };
    }

    private static abstract class LivingHealProxy extends AbstractNeoEventCommon {
        public LivingHealProxy() {
            super(EventType.LIVING_HEAL_EVENT);
        }

        @Override protected void registerToNeo() { NeoForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToNeo() { NeoForge.EVENT_BUS.unregister(this); }
        @Override protected NeoEvent getNeoEventType(Event event) { return new NeoLivingHealEvent(event); }

        protected void handle(LivingHealEvent event) { super.onEvent(event); }
    }

    public static class LivingHealProxyHighest extends LivingHealProxy {
        static final LivingHealProxyHighest INSTANCE = new LivingHealProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(LivingHealEvent e) { handle(e); }
    }

    public static class LivingHealProxyHigh extends LivingHealProxy {
        static final LivingHealProxyHigh INSTANCE = new LivingHealProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(LivingHealEvent e) { handle(e); }
    }

    public static class LivingHealProxyNormal extends LivingHealProxy {
        static final LivingHealProxyNormal INSTANCE = new LivingHealProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(LivingHealEvent e) { handle(e); }
    }

    public static class LivingHealProxyLow extends LivingHealProxy {
        static final LivingHealProxyLow INSTANCE = new LivingHealProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(LivingHealEvent e) { handle(e); }
    }

    public static class LivingHealProxyLowest extends LivingHealProxy {
        static final LivingHealProxyLowest INSTANCE = new LivingHealProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(LivingHealEvent e) { handle(e); }
    }
}
