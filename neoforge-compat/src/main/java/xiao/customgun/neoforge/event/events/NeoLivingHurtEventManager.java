/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforge.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.NeoLivingHurtEvent;

public class NeoLivingHurtEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> NeoLivingHurtProxyHighest.INSTANCE;
            case HIGH -> NeoLivingHurtProxyHigh.INSTANCE;
            case NORMAL -> NeoLivingHurtProxyNormal.INSTANCE;
            case LOW -> NeoLivingHurtProxyLow.INSTANCE;
            case LOWEST -> NeoLivingHurtProxyLowest.INSTANCE;
        };
    }

    private static abstract class NeoLivingHurtProxy extends AbstractNeoEventCommon {
        public NeoLivingHurtProxy() {
            super(EventType.LIVING_HURT_EVENT);
        }

        @Override protected void registerToNeo() { NeoForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToNeo() { NeoForge.EVENT_BUS.unregister(this); }
        @Override protected NeoEvent getNeoEventType(Event event) { return new NeoLivingHurtEvent(event); }

        protected void handle(LivingDamageEvent.Pre event) { super.onEvent(event); }
    }

    public static class NeoLivingHurtProxyHighest extends NeoLivingHurtProxy {
        static final NeoLivingHurtProxyHighest INSTANCE = new NeoLivingHurtProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(LivingDamageEvent.Pre e) { handle(e); }
    }

    public static class NeoLivingHurtProxyHigh extends NeoLivingHurtProxy {
        static final NeoLivingHurtProxyHigh INSTANCE = new NeoLivingHurtProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(LivingDamageEvent.Pre e) { handle(e); }
    }

    public static class NeoLivingHurtProxyNormal extends NeoLivingHurtProxy {
        static final NeoLivingHurtProxyNormal INSTANCE = new NeoLivingHurtProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(LivingDamageEvent.Pre e) { handle(e); }
    }

    public static class NeoLivingHurtProxyLow extends NeoLivingHurtProxy {
        static final NeoLivingHurtProxyLow INSTANCE = new NeoLivingHurtProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(LivingDamageEvent.Pre e) { handle(e); }
    }

    public static class NeoLivingHurtProxyLowest extends NeoLivingHurtProxy {
        static final NeoLivingHurtProxyLowest INSTANCE = new NeoLivingHurtProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(LivingDamageEvent.Pre e) { handle(e); }
    }
}
