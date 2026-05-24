package xiao.customgun.forge.event.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.ForgePlayerStartTrackingEvent;

public class PlayerStartTrackingEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PlayerStartTrackingProxyHighest.INSTANCE;
            case HIGH -> PlayerStartTrackingProxyHigh.INSTANCE;
            case NORMAL -> PlayerStartTrackingProxyNormal.INSTANCE;
            case LOW -> PlayerStartTrackingProxyLow.INSTANCE;
            case LOWEST -> PlayerStartTrackingProxyLowest.INSTANCE;
        };
    }

    private static abstract class PlayerStartTrackingProxy extends AbstractEventCommon {
        public PlayerStartTrackingProxy() {
            super(EventType.PLAYER_START_TRACKING_EVENT);
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
            return new ForgePlayerStartTrackingEvent(event);
        }

        protected void handle(PlayerEvent.StartTracking event) {
            super.onEvent(event);
        }
    }

    public static class PlayerStartTrackingProxyHighest extends PlayerStartTrackingProxy {
        static final PlayerStartTrackingProxyHighest INSTANCE = new PlayerStartTrackingProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(PlayerEvent.StartTracking e) { handle(e); }
    }

    public static class PlayerStartTrackingProxyHigh extends PlayerStartTrackingProxy {
        static final PlayerStartTrackingProxyHigh INSTANCE = new PlayerStartTrackingProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(PlayerEvent.StartTracking e) { handle(e); }
    }

    public static class PlayerStartTrackingProxyNormal extends PlayerStartTrackingProxy {
        static final PlayerStartTrackingProxyNormal INSTANCE = new PlayerStartTrackingProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(PlayerEvent.StartTracking e) { handle(e); }
    }

    public static class PlayerStartTrackingProxyLow extends PlayerStartTrackingProxy {
        static final PlayerStartTrackingProxyLow INSTANCE = new PlayerStartTrackingProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(PlayerEvent.StartTracking e) { handle(e); }
    }

    public static class PlayerStartTrackingProxyLowest extends PlayerStartTrackingProxy {
        static final PlayerStartTrackingProxyLowest INSTANCE = new PlayerStartTrackingProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(PlayerEvent.StartTracking e) { handle(e); }
    }
}