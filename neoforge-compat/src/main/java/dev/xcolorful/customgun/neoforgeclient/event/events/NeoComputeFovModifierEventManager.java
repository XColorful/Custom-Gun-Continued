package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoComputeFovModifierEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoComputeFovModifierEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ComputeFovModifierProxyHighest.INSTANCE;
            case HIGH -> ComputeFovModifierProxyHigh.INSTANCE;
            case NORMAL -> ComputeFovModifierProxyNormal.INSTANCE;
            case LOW -> ComputeFovModifierProxyLow.INSTANCE;
            case LOWEST -> ComputeFovModifierProxyLowest.INSTANCE;
        };
    }

    private static abstract class ComputeFovModifierProxy extends AbstractNeoEventCommon {
        public ComputeFovModifierProxy() {
            super(EventType.COMPUTE_FOV_MODIFIER_EVENT);
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
            return new NeoComputeFovModifierEvent(event);
        }

        protected void handle(ComputeFovModifierEvent event) {
            super.onEvent(event);
        }
    }

    public static class ComputeFovModifierProxyHighest extends ComputeFovModifierProxy {
        static final ComputeFovModifierProxyHighest INSTANCE = new ComputeFovModifierProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(ComputeFovModifierEvent e) { handle(e); }
    }

    public static class ComputeFovModifierProxyHigh extends ComputeFovModifierProxy {
        static final ComputeFovModifierProxyHigh INSTANCE = new ComputeFovModifierProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(ComputeFovModifierEvent e) { handle(e); }
    }

    public static class ComputeFovModifierProxyNormal extends ComputeFovModifierProxy {
        static final ComputeFovModifierProxyNormal INSTANCE = new ComputeFovModifierProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(ComputeFovModifierEvent e) { handle(e); }
    }

    public static class ComputeFovModifierProxyLow extends ComputeFovModifierProxy {
        static final ComputeFovModifierProxyLow INSTANCE = new ComputeFovModifierProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(ComputeFovModifierEvent e) { handle(e); }
    }

    public static class ComputeFovModifierProxyLowest extends ComputeFovModifierProxy {
        static final ComputeFovModifierProxyLowest INSTANCE = new ComputeFovModifierProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(ComputeFovModifierEvent e) { handle(e); }
    }
}