package xiao.customgun.neoforgeclient.event.events;

import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.events.AbstractNeoEventCommon;
import xiao.customgun.neoforgeclient.event.NeoMouseScrollingEvent;

public class NeoMouseScrollingEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> MouseScrollingProxyHighest.INSTANCE;
            case HIGH -> MouseScrollingProxyHigh.INSTANCE;
            case NORMAL -> MouseScrollingProxyNormal.INSTANCE;
            case LOW -> MouseScrollingProxyLow.INSTANCE;
            case LOWEST -> MouseScrollingProxyLowest.INSTANCE;
        };
    }

    private static abstract class MouseScrollingProxy extends AbstractNeoEventCommon {
        public MouseScrollingProxy() {
            super(EventType.MOUSE_SCROLLING_EVENT);
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
            return new NeoMouseScrollingEvent(event);
        }

        protected void handle(InputEvent.MouseScrollingEvent event) {
            super.onEvent(event);
        }
    }

    public static class MouseScrollingProxyHighest extends MouseScrollingProxy {
        static final MouseScrollingProxyHighest INSTANCE = new MouseScrollingProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(InputEvent.MouseScrollingEvent e) { handle(e); }
    }

    public static class MouseScrollingProxyHigh extends MouseScrollingProxy {
        static final MouseScrollingProxyHigh INSTANCE = new MouseScrollingProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(InputEvent.MouseScrollingEvent e) { handle(e); }
    }

    public static class MouseScrollingProxyNormal extends MouseScrollingProxy {
        static final MouseScrollingProxyNormal INSTANCE = new MouseScrollingProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(InputEvent.MouseScrollingEvent e) { handle(e); }
    }

    public static class MouseScrollingProxyLow extends MouseScrollingProxy {
        static final MouseScrollingProxyLow INSTANCE = new MouseScrollingProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(InputEvent.MouseScrollingEvent e) { handle(e); }
    }

    public static class MouseScrollingProxyLowest extends MouseScrollingProxy {
        static final MouseScrollingProxyLowest INSTANCE = new MouseScrollingProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(InputEvent.MouseScrollingEvent e) { handle(e); }
    }
}
