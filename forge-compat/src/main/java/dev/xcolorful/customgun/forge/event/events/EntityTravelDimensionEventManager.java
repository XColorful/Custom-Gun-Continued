package dev.xcolorful.customgun.forge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEntityTravelDimensionEvent;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EntityTravelDimensionEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> EntityTravelDimensionProxyHighest.INSTANCE;
            case HIGH -> EntityTravelDimensionProxyHigh.INSTANCE;
            case NORMAL -> EntityTravelDimensionProxyNormal.INSTANCE;
            case LOW -> EntityTravelDimensionProxyLow.INSTANCE;
            case LOWEST -> EntityTravelDimensionProxyLowest.INSTANCE;
        };
    }

    private static abstract class EntityTravelDimensionProxy extends AbstractEventCommon {
        public EntityTravelDimensionProxy() {
            super(EventType.ENTITY_TRAVEL_DIMENSION_EVENT);
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
            return new ForgeEntityTravelDimensionEvent(event);
        }

        protected void handle(EntityTravelToDimensionEvent event) {
            super.onEvent(event);
        }
    }

    public static class EntityTravelDimensionProxyHighest extends EntityTravelDimensionProxy {
        static final EntityTravelDimensionProxyHighest INSTANCE = new EntityTravelDimensionProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(EntityTravelToDimensionEvent e) { handle(e); }
    }

    public static class EntityTravelDimensionProxyHigh extends EntityTravelDimensionProxy {
        static final EntityTravelDimensionProxyHigh INSTANCE = new EntityTravelDimensionProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(EntityTravelToDimensionEvent e) { handle(e); }
    }

    public static class EntityTravelDimensionProxyNormal extends EntityTravelDimensionProxy {
        static final EntityTravelDimensionProxyNormal INSTANCE = new EntityTravelDimensionProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(EntityTravelToDimensionEvent e) { handle(e); }
    }

    public static class EntityTravelDimensionProxyLow extends EntityTravelDimensionProxy {
        static final EntityTravelDimensionProxyLow INSTANCE = new EntityTravelDimensionProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(EntityTravelToDimensionEvent e) { handle(e); }
    }

    public static class EntityTravelDimensionProxyLowest extends EntityTravelDimensionProxy {
        static final EntityTravelDimensionProxyLowest INSTANCE = new EntityTravelDimensionProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(EntityTravelToDimensionEvent e) { handle(e); }
    }
}