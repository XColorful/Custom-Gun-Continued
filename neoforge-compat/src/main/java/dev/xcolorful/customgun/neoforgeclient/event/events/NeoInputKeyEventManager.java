package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoInputKeyEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoInputKeyEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> InputKeyProxyHighest.INSTANCE;
            case HIGH -> InputKeyProxyHigh.INSTANCE;
            case NORMAL -> InputKeyProxyNormal.INSTANCE;
            case LOW -> InputKeyProxyLow.INSTANCE;
            case LOWEST -> InputKeyProxyLowest.INSTANCE;
        };
    }

    private static abstract class InputKeyProxy extends AbstractNeoEventCommon {
        public InputKeyProxy() {
            super(EventType.INPUT_KEY_EVENT);
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
            return new NeoInputKeyEvent(event);
        }

        protected void handle(InputEvent.Key event) {
            super.onEvent(event);
        }
    }

    public static class InputKeyProxyHighest extends InputKeyProxy {
        static final InputKeyProxyHighest INSTANCE = new InputKeyProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(InputEvent.Key e) { handle(e); }
    }

    public static class InputKeyProxyHigh extends InputKeyProxy {
        static final InputKeyProxyHigh INSTANCE = new InputKeyProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(InputEvent.Key e) { handle(e); }
    }

    public static class InputKeyProxyNormal extends InputKeyProxy {
        static final InputKeyProxyNormal INSTANCE = new InputKeyProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(InputEvent.Key e) { handle(e); }
    }

    public static class InputKeyProxyLow extends InputKeyProxy {
        static final InputKeyProxyLow INSTANCE = new InputKeyProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(InputEvent.Key e) { handle(e); }
    }

    public static class InputKeyProxyLowest extends InputKeyProxy {
        static final InputKeyProxyLowest INSTANCE = new InputKeyProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(InputEvent.Key e) { handle(e); }
    }
}
