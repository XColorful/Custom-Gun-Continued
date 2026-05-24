package xiao.customgun.neoforge.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.NeoPlayerStartTrackingEvent;

public class NeoPlayerStartTrackingEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PlayerStartTrackingProxyHighest.INSTANCE;
            case HIGH -> PlayerStartTrackingProxyHigh.INSTANCE;
            case NORMAL -> PlayerStartTrackingProxyNormal.INSTANCE;
            case LOW -> PlayerStartTrackingProxyLow.INSTANCE;
            case LOWEST -> PlayerStartTrackingProxyLowest.INSTANCE;
        };
    }

    private static abstract class PlayerStartTrackingProxy extends AbstractNeoEventCommon {
        public PlayerStartTrackingProxy() {
            super(EventType.PLAYER_START_TRACKING_EVENT);
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
            return new NeoPlayerStartTrackingEvent(event);
        }

        protected void handle(PlayerEvent.StartTracking event) {
            super.onEvent(event);
        }
    }

    public static class PlayerStartTrackingProxyHighest extends PlayerStartTrackingProxy {
        static final PlayerStartTrackingProxyHighest INSTANCE = new PlayerStartTrackingProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(PlayerEvent.StartTracking e) { handle(e); }
    }

    public static class PlayerStartTrackingProxyHigh extends PlayerStartTrackingProxy {
        static final PlayerStartTrackingProxyHigh INSTANCE = new PlayerStartTrackingProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(PlayerEvent.StartTracking e) { handle(e); }
    }

    public static class PlayerStartTrackingProxyNormal extends PlayerStartTrackingProxy {
        static final PlayerStartTrackingProxyNormal INSTANCE = new PlayerStartTrackingProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(PlayerEvent.StartTracking e) { handle(e); }
    }

    public static class PlayerStartTrackingProxyLow extends PlayerStartTrackingProxy {
        static final PlayerStartTrackingProxyLow INSTANCE = new PlayerStartTrackingProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(PlayerEvent.StartTracking e) { handle(e); }
    }

    public static class PlayerStartTrackingProxyLowest extends PlayerStartTrackingProxy {
        static final PlayerStartTrackingProxyLowest INSTANCE = new PlayerStartTrackingProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(PlayerEvent.StartTracking e) { handle(e); }
    }
}