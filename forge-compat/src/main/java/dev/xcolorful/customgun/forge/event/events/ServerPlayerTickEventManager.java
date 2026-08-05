package dev.xcolorful.customgun.forge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.ForgeServerPlayerTickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerPlayerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ServerPlayerTickProxyHighest.INSTANCE;
            case HIGH -> ServerPlayerTickProxyHigh.INSTANCE;
            case NORMAL -> ServerPlayerTickProxyNormal.INSTANCE;
            case LOW -> ServerPlayerTickProxyLow.INSTANCE;
            case LOWEST -> ServerPlayerTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ServerPlayerTickProxy extends AbstractEventCommon {
        public ServerPlayerTickProxy() {
            super(EventType.SERVER_PLAYER_TICK_EVENT);
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
            return new ForgeServerPlayerTickEvent(event);
        }

        protected void handle(TickEvent.PlayerTickEvent.Post event) {
            if (event.side.isServer()) {
                super.onEvent(event);
            }
        }
    }

    public static class ServerPlayerTickProxyHighest extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyHighest INSTANCE = new ServerPlayerTickProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent.Post e) { handle(e); }
    }

    public static class ServerPlayerTickProxyHigh extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyHigh INSTANCE = new ServerPlayerTickProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent.Post e) { handle(e); }
    }

    public static class ServerPlayerTickProxyNormal extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyNormal INSTANCE = new ServerPlayerTickProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent.Post e) { handle(e); }
    }

    public static class ServerPlayerTickProxyLow extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyLow INSTANCE = new ServerPlayerTickProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent.Post e) { handle(e); }
    }

    public static class ServerPlayerTickProxyLowest extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyLowest INSTANCE = new ServerPlayerTickProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent.Post e) { handle(e); }
    }
}
