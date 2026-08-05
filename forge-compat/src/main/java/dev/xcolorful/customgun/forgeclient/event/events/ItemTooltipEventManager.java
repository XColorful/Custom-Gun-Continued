/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgeItemTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemTooltipEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ItemTooltipProxyHighest.INSTANCE;
            case HIGH -> ItemTooltipProxyHigh.INSTANCE;
            case NORMAL -> ItemTooltipProxyNormal.INSTANCE;
            case LOW -> ItemTooltipProxyLow.INSTANCE;
            case LOWEST -> ItemTooltipProxyLowest.INSTANCE;
        };
    }

    private static abstract class ItemTooltipProxy extends AbstractEventCommon {
        public ItemTooltipProxy() {
            super(EventType.ITEM_TOOLTIP_EVENT);
        }

        @Override protected void registerToForge() { MinecraftForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToForge() { MinecraftForge.EVENT_BUS.unregister(this); }
        @Override protected ForgeEvent getForgeEventType(Event event) { return new ForgeItemTooltipEvent(event); }

        protected void handle(ItemTooltipEvent event) { super.onEvent(event); }
    }

    public static class ItemTooltipProxyHighest extends ItemTooltipProxy {
        static final ItemTooltipProxyHighest INSTANCE = new ItemTooltipProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(ItemTooltipEvent e) { handle(e); }
    }

    public static class ItemTooltipProxyHigh extends ItemTooltipProxy {
        static final ItemTooltipProxyHigh INSTANCE = new ItemTooltipProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(ItemTooltipEvent e) { handle(e); }
    }

    public static class ItemTooltipProxyNormal extends ItemTooltipProxy {
        static final ItemTooltipProxyNormal INSTANCE = new ItemTooltipProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(ItemTooltipEvent e) { handle(e); }
    }

    public static class ItemTooltipProxyLow extends ItemTooltipProxy {
        static final ItemTooltipProxyLow INSTANCE = new ItemTooltipProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(ItemTooltipEvent e) { handle(e); }
    }

    public static class ItemTooltipProxyLowest extends ItemTooltipProxy {
        static final ItemTooltipProxyLowest INSTANCE = new ItemTooltipProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(ItemTooltipEvent e) { handle(e); }
    }
}