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
import dev.xcolorful.customgun.neoforge.event.NeoLivingHurtEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

public class NeoLivingHurtEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> LivingHurtProxyHighest.INSTANCE;
            case HIGH -> LivingHurtProxyHigh.INSTANCE;
            case NORMAL -> LivingHurtProxyNormal.INSTANCE;
            case LOW -> LivingHurtProxyLow.INSTANCE;
            case LOWEST -> LivingHurtProxyLowest.INSTANCE;
        };
    }

    private static abstract class LivingHurtProxy extends AbstractNeoEventCommon {
        public LivingHurtProxy() {
            super(EventType.LIVING_HURT_EVENT);
        }

        @Override protected void registerToNeo() { NeoForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToNeo() { NeoForge.EVENT_BUS.unregister(this); }
        @Override protected NeoEvent getNeoEventType(Event event) { return new NeoLivingHurtEvent(event); }

        protected void handle(LivingDamageEvent.Pre event) { super.onEvent(event); }
    }

    public static class LivingHurtProxyHighest extends LivingHurtProxy {
        static final LivingHurtProxyHighest INSTANCE = new LivingHurtProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(LivingDamageEvent.Pre e) { handle(e); }
    }

    public static class LivingHurtProxyHigh extends LivingHurtProxy {
        static final LivingHurtProxyHigh INSTANCE = new LivingHurtProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(LivingDamageEvent.Pre e) { handle(e); }
    }

    public static class LivingHurtProxyNormal extends LivingHurtProxy {
        static final LivingHurtProxyNormal INSTANCE = new LivingHurtProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(LivingDamageEvent.Pre e) { handle(e); }
    }

    public static class LivingHurtProxyLow extends LivingHurtProxy {
        static final LivingHurtProxyLow INSTANCE = new LivingHurtProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(LivingDamageEvent.Pre e) { handle(e); }
    }

    public static class LivingHurtProxyLowest extends LivingHurtProxy {
        static final LivingHurtProxyLowest INSTANCE = new LivingHurtProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(LivingDamageEvent.Pre e) { handle(e); }
    }
}
