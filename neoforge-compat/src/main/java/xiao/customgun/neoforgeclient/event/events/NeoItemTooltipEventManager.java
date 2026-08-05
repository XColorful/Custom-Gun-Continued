/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoItemTooltipEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class NeoItemTooltipEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ItemTooltipProxyHighest.INSTANCE;
            case HIGH -> ItemTooltipProxyHigh.INSTANCE;
            case NORMAL -> ItemTooltipProxyNormal.INSTANCE;
            case LOW -> ItemTooltipProxyLow.INSTANCE;
            case LOWEST -> ItemTooltipProxyLowest.INSTANCE;
        };
    }

    private static abstract class ItemTooltipProxy extends AbstractNeoEventCommon {
        public ItemTooltipProxy() {
            super(EventType.ITEM_TOOLTIP_EVENT);
        }

        @Override protected void registerToNeo() { NeoForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToNeo() { NeoForge.EVENT_BUS.unregister(this); }
        @Override protected NeoEvent getNeoEventType(Event event) { return new NeoItemTooltipEvent(event); }

        protected void handle(ItemTooltipEvent event) { super.onEvent(event); }
    }

    public static class ItemTooltipProxyHighest extends ItemTooltipProxy {
        static final ItemTooltipProxyHighest INSTANCE = new ItemTooltipProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(ItemTooltipEvent e) { handle(e); }
    }

    public static class ItemTooltipProxyHigh extends ItemTooltipProxy {
        static final ItemTooltipProxyHigh INSTANCE = new ItemTooltipProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(ItemTooltipEvent e) { handle(e); }
    }

    public static class ItemTooltipProxyNormal extends ItemTooltipProxy {
        static final ItemTooltipProxyNormal INSTANCE = new ItemTooltipProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(ItemTooltipEvent e) { handle(e); }
    }

    public static class ItemTooltipProxyLow extends ItemTooltipProxy {
        static final ItemTooltipProxyLow INSTANCE = new ItemTooltipProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(ItemTooltipEvent e) { handle(e); }
    }

    public static class ItemTooltipProxyLowest extends ItemTooltipProxy {
        static final ItemTooltipProxyLowest INSTANCE = new ItemTooltipProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(ItemTooltipEvent e) { handle(e); }
    }
}
