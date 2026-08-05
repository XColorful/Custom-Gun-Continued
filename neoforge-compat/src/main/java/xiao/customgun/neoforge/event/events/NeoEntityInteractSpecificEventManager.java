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
import dev.xcolorful.customgun.neoforge.event.NeoEntityInteractSpecificEvent;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class NeoEntityInteractSpecificEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> EntityInteractSpecificProxyHighest.INSTANCE;
            case HIGH -> EntityInteractSpecificProxyHigh.INSTANCE;
            case NORMAL -> EntityInteractSpecificProxyNormal.INSTANCE;
            case LOW -> EntityInteractSpecificProxyLow.INSTANCE;
            case LOWEST -> EntityInteractSpecificProxyLowest.INSTANCE;
        };
    }

    private static abstract class EntityInteractSpecificProxy extends AbstractNeoEventCommon {
        public EntityInteractSpecificProxy() {
            super(EventType.ENTITY_INTERACT_SPECIFIC_EVENT);
        }

        @Override protected void registerToNeo() { NeoForge.EVENT_BUS.register(this); }
        @Override protected void unregisterToNeo() { NeoForge.EVENT_BUS.unregister(this); }
        @Override protected NeoEvent getNeoEventType(Event event) { return new NeoEntityInteractSpecificEvent(event); }

        protected void handle(PlayerInteractEvent.EntityInteractSpecific event) { super.onEvent(event); }
    }

    public static class EntityInteractSpecificProxyHighest extends EntityInteractSpecificProxy {
        static final EntityInteractSpecificProxyHighest INSTANCE = new EntityInteractSpecificProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.EntityInteractSpecific e) { handle(e); }
    }

    public static class EntityInteractSpecificProxyHigh extends EntityInteractSpecificProxy {
        static final EntityInteractSpecificProxyHigh INSTANCE = new EntityInteractSpecificProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.EntityInteractSpecific e) { handle(e); }
    }

    public static class EntityInteractSpecificProxyNormal extends EntityInteractSpecificProxy {
        static final EntityInteractSpecificProxyNormal INSTANCE = new EntityInteractSpecificProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.EntityInteractSpecific e) { handle(e); }
    }

    public static class EntityInteractSpecificProxyLow extends EntityInteractSpecificProxy {
        static final EntityInteractSpecificProxyLow INSTANCE = new EntityInteractSpecificProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.EntityInteractSpecific e) { handle(e); }
    }

    public static class EntityInteractSpecificProxyLowest extends EntityInteractSpecificProxy {
        static final EntityInteractSpecificProxyLowest INSTANCE = new EntityInteractSpecificProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(PlayerInteractEvent.EntityInteractSpecific e) { handle(e); }
    }
}
