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
import dev.xcolorful.customgun.neoforge.event.NeoLivingAttackEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;

public class NeoLivingAttackEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> LivingAttackProxyHighest.INSTANCE;
            case HIGH -> LivingAttackProxyHigh.INSTANCE;
            case NORMAL -> LivingAttackProxyNormal.INSTANCE;
            case LOW -> LivingAttackProxyLow.INSTANCE;
            case LOWEST -> LivingAttackProxyLowest.INSTANCE;
        };
    }

    private static abstract class LivingAttackProxy extends AbstractNeoEventCommon {
        public LivingAttackProxy() {
            super(EventType.LIVING_ATTACK_EVENT);
        }

        @Override protected void registerToNeo() { NeoForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToNeo() { NeoForge.EVENT_BUS.unregister(this); }
        @Override protected NeoEvent getNeoEventType(Event event) { return new NeoLivingAttackEvent(event); }

        protected void handle(LivingAttackEvent event) { super.onEvent(event); }
    }

    public static class LivingAttackProxyHighest extends LivingAttackProxy {
        static final LivingAttackProxyHighest INSTANCE = new LivingAttackProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(LivingAttackEvent e) { handle(e); }
    }

    public static class LivingAttackProxyHigh extends LivingAttackProxy {
        static final LivingAttackProxyHigh INSTANCE = new LivingAttackProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(LivingAttackEvent e) { handle(e); }
    }

    public static class LivingAttackProxyNormal extends LivingAttackProxy {
        static final LivingAttackProxyNormal INSTANCE = new LivingAttackProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(LivingAttackEvent e) { handle(e); }
    }

    public static class LivingAttackProxyLow extends LivingAttackProxy {
        static final LivingAttackProxyLow INSTANCE = new LivingAttackProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(LivingAttackEvent e) { handle(e); }
    }

    public static class LivingAttackProxyLowest extends LivingAttackProxy {
        static final LivingAttackProxyLowest INSTANCE = new LivingAttackProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(LivingAttackEvent e) { handle(e); }
    }
}
