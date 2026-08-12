package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoComputeFovEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoComputeFovEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ComputeFovProxyHighest.INSTANCE;
            case HIGH -> ComputeFovProxyHigh.INSTANCE;
            case NORMAL -> ComputeFovProxyNormal.INSTANCE;
            case LOW -> ComputeFovProxyLow.INSTANCE;
            case LOWEST -> ComputeFovProxyLowest.INSTANCE;
        };
    }

    private static abstract class ComputeFovProxy extends AbstractNeoEventCommon {
        public ComputeFovProxy() {
            super(EventType.COMPUTE_FOV_EVENT);
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
            return new NeoComputeFovEvent(event);
        }

        protected void handle(ViewportEvent.ComputeFov event) {
            super.onEvent(event);
        }
    }

    public static class ComputeFovProxyHighest extends ComputeFovProxy {
        static final ComputeFovProxyHighest INSTANCE = new ComputeFovProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeFov e) { handle(e); }
    }

    public static class ComputeFovProxyHigh extends ComputeFovProxy {
        static final ComputeFovProxyHigh INSTANCE = new ComputeFovProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeFov e) { handle(e); }
    }

    public static class ComputeFovProxyNormal extends ComputeFovProxy {
        static final ComputeFovProxyNormal INSTANCE = new ComputeFovProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeFov e) { handle(e); }
    }

    public static class ComputeFovProxyLow extends ComputeFovProxy {
        static final ComputeFovProxyLow INSTANCE = new ComputeFovProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeFov e) { handle(e); }
    }

    public static class ComputeFovProxyLowest extends ComputeFovProxy {
        static final ComputeFovProxyLowest INSTANCE = new ComputeFovProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeFov e) { handle(e); }
    }
}