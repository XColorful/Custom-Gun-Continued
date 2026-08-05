package dev.xcolorful.customgun.neoforge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.NeoServerPlayerTickEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class NeoServerPlayerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ServerPlayerTickProxyHighest.INSTANCE;
            case HIGH -> ServerPlayerTickProxyHigh.INSTANCE;
            case NORMAL -> ServerPlayerTickProxyNormal.INSTANCE;
            case LOW -> ServerPlayerTickProxyLow.INSTANCE;
            case LOWEST -> ServerPlayerTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ServerPlayerTickProxy extends AbstractNeoEventCommon {
        public ServerPlayerTickProxy() {
            super(EventType.SERVER_PLAYER_TICK_EVENT);
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
            return new NeoServerPlayerTickEvent(event);
        }

        protected void handle(PlayerTickEvent.Post event) {
            if (!event.getEntity().level().isClientSide()) {
                super.onEvent(event);
            }
        }
    }

    public static class ServerPlayerTickProxyHighest extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyHighest INSTANCE = new ServerPlayerTickProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(PlayerTickEvent.Post e) { handle(e); }
    }

    public static class ServerPlayerTickProxyHigh extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyHigh INSTANCE = new ServerPlayerTickProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(PlayerTickEvent.Post e) { handle(e); }
    }

    public static class ServerPlayerTickProxyNormal extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyNormal INSTANCE = new ServerPlayerTickProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(PlayerTickEvent.Post e) { handle(e); }
    }

    public static class ServerPlayerTickProxyLow extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyLow INSTANCE = new ServerPlayerTickProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(PlayerTickEvent.Post e) { handle(e); }
    }

    public static class ServerPlayerTickProxyLowest extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyLowest INSTANCE = new ServerPlayerTickProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(PlayerTickEvent.Post e) { handle(e); }
    }
}
