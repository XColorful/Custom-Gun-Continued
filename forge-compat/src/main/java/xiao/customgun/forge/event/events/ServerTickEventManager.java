/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.forge.event.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.ForgeServerTickEvent;

public class ServerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ServerTickProxyHighest.INSTANCE;
            case HIGH -> ServerTickProxyHigh.INSTANCE;
            case NORMAL -> ServerTickProxyNormal.INSTANCE;
            case LOW -> ServerTickProxyLow.INSTANCE;
            case LOWEST -> ServerTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ServerTickProxy extends AbstractEventCommon {
        public ServerTickProxy() {
            super(EventType.SERVER_TICK_EVENT);
        }

        @Override
        protected void registerToForge() {
            MinecraftForge.EVENT_BUS.register(this);
        }

        @Override
        protected void unregisterToForge() {
            MinecraftForge.EVENT_BUS.unregister(this);
        }

        @Override
        protected ForgeEvent getForgeEventType(Event event) {
            return new ForgeServerTickEvent(event);
        }

        protected void handle(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                super.onEvent(event);
            }
        }
    }

    public static class ServerTickProxyHighest extends ServerTickProxy {
        static final ServerTickProxyHighest INSTANCE = new ServerTickProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class ServerTickProxyHigh extends ServerTickProxy {
        static final ServerTickProxyHigh INSTANCE = new ServerTickProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class ServerTickProxyNormal extends ServerTickProxy {
        static final ServerTickProxyNormal INSTANCE = new ServerTickProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class ServerTickProxyLow extends ServerTickProxy {
        static final ServerTickProxyLow INSTANCE = new ServerTickProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class ServerTickProxyLowest extends ServerTickProxy {
        static final ServerTickProxyLowest INSTANCE = new ServerTickProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }
}