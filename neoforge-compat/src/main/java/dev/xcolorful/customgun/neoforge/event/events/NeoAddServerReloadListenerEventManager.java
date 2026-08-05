package dev.xcolorful.customgun.neoforge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoAddServerReloadListenerEvent;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

public class NeoAddServerReloadListenerEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> NeoAddServerReloadListenerProxyHighest.INSTANCE;
            case HIGH -> NeoAddServerReloadListenerProxyHigh.INSTANCE;
            case NORMAL -> NeoAddServerReloadListenerProxyNormal.INSTANCE;
            case LOW -> NeoAddServerReloadListenerProxyLow.INSTANCE;
            case LOWEST -> NeoAddServerReloadListenerProxyLowest.INSTANCE;
        };
    }

    private static abstract class NeoAddServerReloadListenerProxy extends AbstractNeoEventCommon {
        public NeoAddServerReloadListenerProxy() {
            super(EventType.ADD_SERVER_RELOAD_LISTENER_EVENT);
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
            return new NeoAddServerReloadListenerEvent(event);
        }

        protected void handle(AddServerReloadListenersEvent event) {
            super.onEvent(event);
        }
    }

    public static class NeoAddServerReloadListenerProxyHighest extends NeoAddServerReloadListenerProxy {
        static final NeoAddServerReloadListenerProxyHighest INSTANCE = new NeoAddServerReloadListenerProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(AddServerReloadListenersEvent e) { handle(e); }
    }

    public static class NeoAddServerReloadListenerProxyHigh extends NeoAddServerReloadListenerProxy {
        static final NeoAddServerReloadListenerProxyHigh INSTANCE = new NeoAddServerReloadListenerProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(AddServerReloadListenersEvent e) { handle(e); }
    }

    public static class NeoAddServerReloadListenerProxyNormal extends NeoAddServerReloadListenerProxy {
        static final NeoAddServerReloadListenerProxyNormal INSTANCE = new NeoAddServerReloadListenerProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(AddServerReloadListenersEvent e) { handle(e); }
    }

    public static class NeoAddServerReloadListenerProxyLow extends NeoAddServerReloadListenerProxy {
        static final NeoAddServerReloadListenerProxyLow INSTANCE = new NeoAddServerReloadListenerProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(AddServerReloadListenersEvent e) { handle(e); }
    }

    public static class NeoAddServerReloadListenerProxyLowest extends NeoAddServerReloadListenerProxy {
        static final NeoAddServerReloadListenerProxyLowest INSTANCE = new NeoAddServerReloadListenerProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(AddServerReloadListenersEvent e) { handle(e); }
    }
}