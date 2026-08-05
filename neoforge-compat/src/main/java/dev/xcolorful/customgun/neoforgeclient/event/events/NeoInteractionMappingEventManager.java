package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoInteractionMappingEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoInteractionMappingEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> InteractionMappingProxyHighest.INSTANCE;
            case HIGH -> InteractionMappingProxyHigh.INSTANCE;
            case NORMAL -> InteractionMappingProxyNormal.INSTANCE;
            case LOW -> InteractionMappingProxyLow.INSTANCE;
            case LOWEST -> InteractionMappingProxyLowest.INSTANCE;
        };
    }

    private static abstract class InteractionMappingProxy extends AbstractNeoEventCommon {
        public InteractionMappingProxy() {
            super(EventType.INTERACTION_MAPPING_EVENT);
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
            return new NeoInteractionMappingEvent(event);
        }

        protected void handle(InputEvent.InteractionKeyMappingTriggered event) {
            super.onEvent(event);
        }
    }

    public static class InteractionMappingProxyHighest extends InteractionMappingProxy {
        static final InteractionMappingProxyHighest INSTANCE = new InteractionMappingProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(InputEvent.InteractionKeyMappingTriggered e) { handle(e); }
    }

    public static class InteractionMappingProxyHigh extends InteractionMappingProxy {
        static final InteractionMappingProxyHigh INSTANCE = new InteractionMappingProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(InputEvent.InteractionKeyMappingTriggered e) { handle(e); }
    }

    public static class InteractionMappingProxyNormal extends InteractionMappingProxy {
        static final InteractionMappingProxyNormal INSTANCE = new InteractionMappingProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(InputEvent.InteractionKeyMappingTriggered e) { handle(e); }
    }

    public static class InteractionMappingProxyLow extends InteractionMappingProxy {
        static final InteractionMappingProxyLow INSTANCE = new InteractionMappingProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(InputEvent.InteractionKeyMappingTriggered e) { handle(e); }
    }

    public static class InteractionMappingProxyLowest extends InteractionMappingProxy {
        static final InteractionMappingProxyLowest INSTANCE = new InteractionMappingProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(InputEvent.InteractionKeyMappingTriggered e) { handle(e); }
    }
}
