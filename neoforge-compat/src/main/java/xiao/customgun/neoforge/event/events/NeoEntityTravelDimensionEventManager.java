package xiao.customgun.neoforge.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEntityTravelDimensionEvent;
import xiao.customgun.neoforge.event.NeoEvent;

public class NeoEntityTravelDimensionEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> EntityTravelDimensionProxyHighest.INSTANCE;
            case HIGH -> EntityTravelDimensionProxyHigh.INSTANCE;
            case NORMAL -> EntityTravelDimensionProxyNormal.INSTANCE;
            case LOW -> EntityTravelDimensionProxyLow.INSTANCE;
            case LOWEST -> EntityTravelDimensionProxyLowest.INSTANCE;
        };
    }

    private static abstract class EntityTravelDimensionProxy extends AbstractNeoEventCommon {
        public EntityTravelDimensionProxy() {
            super(EventType.ENTITY_TRAVEL_DIMENSION_EVENT);
        }

        @Override
        protected void registerToNeo() {
            NeoForge.EVENT_BUS.register(this);
        }

        @Override
        protected void unregisterToNeo() {
            NeoForge.EVENT_BUS.unregister(this);
        }

        @Override
        protected NeoEvent getNeoEventType(Event event) {
            return new NeoEntityTravelDimensionEvent(event);
        }

        protected void handle(EntityTravelToDimensionEvent event) {
            super.onEvent(event);
        }
    }

    public static class EntityTravelDimensionProxyHighest extends EntityTravelDimensionProxy {
        static final EntityTravelDimensionProxyHighest INSTANCE = new EntityTravelDimensionProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(EntityTravelToDimensionEvent e) { handle(e); }
    }

    public static class EntityTravelDimensionProxyHigh extends EntityTravelDimensionProxy {
        static final EntityTravelDimensionProxyHigh INSTANCE = new EntityTravelDimensionProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(EntityTravelToDimensionEvent e) { handle(e); }
    }

    public static class EntityTravelDimensionProxyNormal extends EntityTravelDimensionProxy {
        static final EntityTravelDimensionProxyNormal INSTANCE = new EntityTravelDimensionProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(EntityTravelToDimensionEvent e) { handle(e); }
    }

    public static class EntityTravelDimensionProxyLow extends EntityTravelDimensionProxy {
        static final EntityTravelDimensionProxyLow INSTANCE = new EntityTravelDimensionProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(EntityTravelToDimensionEvent e) { handle(e); }
    }

    public static class EntityTravelDimensionProxyLowest extends EntityTravelDimensionProxy {
        static final EntityTravelDimensionProxyLowest INSTANCE = new EntityTravelDimensionProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(EntityTravelToDimensionEvent e) { handle(e); }
    }
}
