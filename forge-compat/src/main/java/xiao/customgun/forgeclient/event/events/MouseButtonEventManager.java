package xiao.customgun.forgeclient.event.events;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.events.AbstractEventCommon;
import xiao.customgun.forgeclient.event.ForgeMouseButtonEvent;

public class MouseButtonEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> MouseButtonProxyHighest.INSTANCE;
            case HIGH -> MouseButtonProxyHigh.INSTANCE;
            case NORMAL -> MouseButtonProxyNormal.INSTANCE;
            case LOW -> MouseButtonProxyLow.INSTANCE;
            case LOWEST -> MouseButtonProxyLowest.INSTANCE;
        };
    }

    private static abstract class MouseButtonProxy extends AbstractEventCommon {
        public MouseButtonProxy() {
            super(EventType.MOUSE_BUTTON_EVENT);
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
            return new ForgeMouseButtonEvent(event);
        }

        protected void handle(InputEvent.MouseButton.Pre event) {
            super.onEvent(event);
        }
    }

    public static class MouseButtonProxyHighest extends MouseButtonProxy {
        static final MouseButtonProxyHighest INSTANCE = new MouseButtonProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(InputEvent.MouseButton.Pre e) { handle(e); }
    }

    public static class MouseButtonProxyHigh extends MouseButtonProxy {
        static final MouseButtonProxyHigh INSTANCE = new MouseButtonProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(InputEvent.MouseButton.Pre e) { handle(e); }
    }

    public static class MouseButtonProxyNormal extends MouseButtonProxy {
        static final MouseButtonProxyNormal INSTANCE = new MouseButtonProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(InputEvent.MouseButton.Pre e) { handle(e); }
    }

    public static class MouseButtonProxyLow extends MouseButtonProxy {
        static final MouseButtonProxyLow INSTANCE = new MouseButtonProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(InputEvent.MouseButton.Pre e) { handle(e); }
    }

    public static class MouseButtonProxyLowest extends MouseButtonProxy {
        static final MouseButtonProxyLowest INSTANCE = new MouseButtonProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(InputEvent.MouseButton.Pre e) { handle(e); }
    }
}
