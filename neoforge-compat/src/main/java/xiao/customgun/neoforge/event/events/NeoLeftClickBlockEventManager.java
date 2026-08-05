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
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.NeoLeftClickBlockEvent;

public class NeoLeftClickBlockEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> LeftClickBlockProxyHighest.INSTANCE;
            case HIGH -> LeftClickBlockProxyHigh.INSTANCE;
            case NORMAL -> LeftClickBlockProxyNormal.INSTANCE;
            case LOW -> LeftClickBlockProxyLow.INSTANCE;
            case LOWEST -> LeftClickBlockProxyLowest.INSTANCE;
        };
    }

    private static abstract class LeftClickBlockProxy extends AbstractNeoEventCommon {
        public LeftClickBlockProxy() {
            super(EventType.LEFT_CLICK_BLOCK_EVENT);
        }

        @Override protected void registerToNeo() { NeoForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToNeo() { NeoForge.EVENT_BUS.unregister(this); }
        @Override protected NeoEvent getNeoEventType(Event event) { return new NeoLeftClickBlockEvent(event); }

        protected void handle(PlayerInteractEvent.LeftClickBlock event) { super.onEvent(event); }
    }

    public static class LeftClickBlockProxyHighest extends LeftClickBlockProxy {
        static final LeftClickBlockProxyHighest INSTANCE = new LeftClickBlockProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.LeftClickBlock e) { handle(e); }
    }

    public static class LeftClickBlockProxyHigh extends LeftClickBlockProxy {
        static final LeftClickBlockProxyHigh INSTANCE = new LeftClickBlockProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.LeftClickBlock e) { handle(e); }
    }

    public static class LeftClickBlockProxyNormal extends LeftClickBlockProxy {
        static final LeftClickBlockProxyNormal INSTANCE = new LeftClickBlockProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.LeftClickBlock e) { handle(e); }
    }

    public static class LeftClickBlockProxyLow extends LeftClickBlockProxy {
        static final LeftClickBlockProxyLow INSTANCE = new LeftClickBlockProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.LeftClickBlock e) { handle(e); }
    }

    public static class LeftClickBlockProxyLowest extends LeftClickBlockProxy {
        static final LeftClickBlockProxyLowest INSTANCE = new LeftClickBlockProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.LeftClickBlock e) { handle(e); }
    }
}
