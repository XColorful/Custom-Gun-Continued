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
import xiao.customgun.neoforge.event.NeoRightClickItemEvent;

public class NeoRightClickItemEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RightClickItemProxyHighest.INSTANCE;
            case HIGH -> RightClickItemProxyHigh.INSTANCE;
            case NORMAL -> RightClickItemProxyNormal.INSTANCE;
            case LOW -> RightClickItemProxyLow.INSTANCE;
            case LOWEST -> RightClickItemProxyLowest.INSTANCE;
        };
    }

    private static abstract class RightClickItemProxy extends AbstractNeoEventCommon {
        public RightClickItemProxy() {
            super(EventType.RIGHT_CLICK_ITEM_EVENT);
        }

        @Override protected void registerToNeo() { NeoForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToNeo() { NeoForge.EVENT_BUS.unregister(this); }
        @Override protected NeoEvent getNeoEventType(Event event) { return new NeoRightClickItemEvent(event); }

        protected void handle(PlayerInteractEvent.RightClickItem event) { super.onEvent(event); }
    }

    public static class RightClickItemProxyHighest extends RightClickItemProxy {
        static final RightClickItemProxyHighest INSTANCE = new RightClickItemProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.RightClickItem e) { handle(e); }
    }

    public static class RightClickItemProxyHigh extends RightClickItemProxy {
        static final RightClickItemProxyHigh INSTANCE = new RightClickItemProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.RightClickItem e) { handle(e); }
    }

    public static class RightClickItemProxyNormal extends RightClickItemProxy {
        static final RightClickItemProxyNormal INSTANCE = new RightClickItemProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.RightClickItem e) { handle(e); }
    }

    public static class RightClickItemProxyLow extends RightClickItemProxy {
        static final RightClickItemProxyLow INSTANCE = new RightClickItemProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.RightClickItem e) { handle(e); }
    }

    public static class RightClickItemProxyLowest extends RightClickItemProxy {
        static final RightClickItemProxyLowest INSTANCE = new RightClickItemProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.RightClickItem e) { handle(e); }
    }
}
