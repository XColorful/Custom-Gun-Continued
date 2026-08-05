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
import xiao.customgun.neoforgeclient.event.NeoMouseButtonEvent;

public class NeoMouseButtonEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> MouseButtonProxyHighest.INSTANCE;
            case HIGH -> MouseButtonProxyHigh.INSTANCE;
            case NORMAL -> MouseButtonProxyNormal.INSTANCE;
            case LOW -> MouseButtonProxyLow.INSTANCE;
            case LOWEST -> MouseButtonProxyLowest.INSTANCE;
        };
    }

    private static abstract class MouseButtonProxy extends AbstractNeoEventCommon {
        public MouseButtonProxy() {
            super(EventType.MOUSE_BUTTON_EVENT);
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
            return new NeoMouseButtonEvent(event);
        }

        protected void handle(InputEvent.MouseButton.Pre event) {
            super.onEvent(event);
        }
    }

    public static class MouseButtonProxyHighest extends MouseButtonProxy {
        static final MouseButtonProxyHighest INSTANCE = new MouseButtonProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(InputEvent.MouseButton.Pre e) { handle(e); }
    }

    public static class MouseButtonProxyHigh extends MouseButtonProxy {
        static final MouseButtonProxyHigh INSTANCE = new MouseButtonProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(InputEvent.MouseButton.Pre e) { handle(e); }
    }

    public static class MouseButtonProxyNormal extends MouseButtonProxy {
        static final MouseButtonProxyNormal INSTANCE = new MouseButtonProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(InputEvent.MouseButton.Pre e) { handle(e); }
    }

    public static class MouseButtonProxyLow extends MouseButtonProxy {
        static final MouseButtonProxyLow INSTANCE = new MouseButtonProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(InputEvent.MouseButton.Pre e) { handle(e); }
    }

    public static class MouseButtonProxyLowest extends MouseButtonProxy {
        static final MouseButtonProxyLowest INSTANCE = new MouseButtonProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(InputEvent.MouseButton.Pre e) { handle(e); }
    }
}
