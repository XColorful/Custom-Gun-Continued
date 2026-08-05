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
import xiao.customgun.neoforge.event.NeoRightClickBlockEvent;

public class NeoRightClickBlockEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RightClickBlockProxyHighest.INSTANCE;
            case HIGH -> RightClickBlockProxyHigh.INSTANCE;
            case NORMAL -> RightClickBlockProxyNormal.INSTANCE;
            case LOW -> RightClickBlockProxyLow.INSTANCE;
            case LOWEST -> RightClickBlockProxyLowest.INSTANCE;
        };
    }

    private static abstract class RightClickBlockProxy extends AbstractNeoEventCommon {
        public RightClickBlockProxy() {
            super(EventType.RIGHT_CLICK_BLOCK_EVENT);
        }

        @Override protected void registerToNeo() { NeoForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToNeo() { NeoForge.EVENT_BUS.unregister(this); }
        @Override protected NeoEvent getNeoEventType(Event event) { return new NeoRightClickBlockEvent(event); }

        protected void handle(PlayerInteractEvent.RightClickBlock event) { super.onEvent(event); }
    }

    public static class RightClickBlockProxyHighest extends RightClickBlockProxy {
        static final RightClickBlockProxyHighest INSTANCE = new RightClickBlockProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.RightClickBlock e) { handle(e); }
    }

    public static class RightClickBlockProxyHigh extends RightClickBlockProxy {
        static final RightClickBlockProxyHigh INSTANCE = new RightClickBlockProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.RightClickBlock e) { handle(e); }
    }

    public static class RightClickBlockProxyNormal extends RightClickBlockProxy {
        static final RightClickBlockProxyNormal INSTANCE = new RightClickBlockProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.RightClickBlock e) { handle(e); }
    }

    public static class RightClickBlockProxyLow extends RightClickBlockProxy {
        static final RightClickBlockProxyLow INSTANCE = new RightClickBlockProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.RightClickBlock e) { handle(e); }
    }

    public static class RightClickBlockProxyLowest extends RightClickBlockProxy {
        static final RightClickBlockProxyLowest INSTANCE = new RightClickBlockProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.RightClickBlock e) { handle(e); }
    }
}
