/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.forge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.ForgeLivingUseTotemEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingUseTotemEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LivingUseTotemEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> LivingUseTotemProxyHighest.INSTANCE;
            case HIGH -> LivingUseTotemProxyHigh.INSTANCE;
            case NORMAL -> LivingUseTotemProxyNormal.INSTANCE;
            case LOW -> LivingUseTotemProxyLow.INSTANCE;
            case LOWEST -> LivingUseTotemProxyLowest.INSTANCE;
        };
    }

    private static abstract class LivingUseTotemProxy extends AbstractEventCommon {
        public LivingUseTotemProxy() {
            super(EventType.LIVING_USE_TOTEM_EVENT);
        }

        @Override protected void registerToForge() { MinecraftForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToForge() { MinecraftForge.EVENT_BUS.unregister(this); }
        @Override protected ForgeEvent getForgeEventType(Event event) { return new ForgeLivingUseTotemEvent(event); }

        protected void handle(LivingUseTotemEvent event) { super.onEvent(event); }
    }

    public static class LivingUseTotemProxyHighest extends LivingUseTotemProxy {
        static final LivingUseTotemProxyHighest INSTANCE = new LivingUseTotemProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(LivingUseTotemEvent e) { handle(e); }
    }

    public static class LivingUseTotemProxyHigh extends LivingUseTotemProxy {
        static final LivingUseTotemProxyHigh INSTANCE = new LivingUseTotemProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(LivingUseTotemEvent e) { handle(e); }
    }

    public static class LivingUseTotemProxyNormal extends LivingUseTotemProxy {
        static final LivingUseTotemProxyNormal INSTANCE = new LivingUseTotemProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(LivingUseTotemEvent e) { handle(e); }
    }

    public static class LivingUseTotemProxyLow extends LivingUseTotemProxy {
        static final LivingUseTotemProxyLow INSTANCE = new LivingUseTotemProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(LivingUseTotemEvent e) { handle(e); }
    }

    public static class LivingUseTotemProxyLowest extends LivingUseTotemProxy {
        static final LivingUseTotemProxyLowest INSTANCE = new LivingUseTotemProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(LivingUseTotemEvent e) { handle(e); }
    }
}