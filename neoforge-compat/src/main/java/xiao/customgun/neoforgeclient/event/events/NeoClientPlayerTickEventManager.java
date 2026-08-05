package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoClientPlayerTickEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;

public class NeoClientPlayerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ClientPlayerTickProxyHighest.INSTANCE;
            case HIGH -> ClientPlayerTickProxyHigh.INSTANCE;
            case NORMAL -> ClientPlayerTickProxyNormal.INSTANCE;
            case LOW -> ClientPlayerTickProxyLow.INSTANCE;
            case LOWEST -> ClientPlayerTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ClientPlayerTickProxy extends AbstractNeoEventCommon {
        public ClientPlayerTickProxy() {
            super(EventType.CLIENT_PLAYER_TICK_EVENT);
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
            return new NeoClientPlayerTickEvent(event);
        }

        protected void handle(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            if (event.side.isClient()) {
                super.onEvent(event);
            }
        }
    }

    public static class ClientPlayerTickProxyHighest extends ClientPlayerTickProxy {
        static final ClientPlayerTickProxyHighest INSTANCE = new ClientPlayerTickProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ClientPlayerTickProxyHigh extends ClientPlayerTickProxy {
        static final ClientPlayerTickProxyHigh INSTANCE = new ClientPlayerTickProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ClientPlayerTickProxyNormal extends ClientPlayerTickProxy {
        static final ClientPlayerTickProxyNormal INSTANCE = new ClientPlayerTickProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ClientPlayerTickProxyLow extends ClientPlayerTickProxy {
        static final ClientPlayerTickProxyLow INSTANCE = new ClientPlayerTickProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ClientPlayerTickProxyLowest extends ClientPlayerTickProxy {
        static final ClientPlayerTickProxyLowest INSTANCE = new ClientPlayerTickProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }
}
