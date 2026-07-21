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
import xiao.customgun.forgeclient.event.ForgeMouseScrollingEvent;

public class MouseScrollingEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> MouseScrollingProxyHighest.INSTANCE;
            case HIGH -> MouseScrollingProxyHigh.INSTANCE;
            case NORMAL -> MouseScrollingProxyNormal.INSTANCE;
            case LOW -> MouseScrollingProxyLow.INSTANCE;
            case LOWEST -> MouseScrollingProxyLowest.INSTANCE;
        };
    }

    private static abstract class MouseScrollingProxy extends AbstractEventCommon {
        public MouseScrollingProxy() {
            super(EventType.MOUSE_SCROLLING_EVENT);
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
            return new ForgeMouseScrollingEvent(event);
        }

        protected void handle(InputEvent.MouseScrollingEvent event) {
            super.onEvent(event);
        }
    }

    public static class MouseScrollingProxyHighest extends MouseScrollingProxy {
        static final MouseScrollingProxyHighest INSTANCE = new MouseScrollingProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(InputEvent.MouseScrollingEvent e) { handle(e); }
    }

    public static class MouseScrollingProxyHigh extends MouseScrollingProxy {
        static final MouseScrollingProxyHigh INSTANCE = new MouseScrollingProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(InputEvent.MouseScrollingEvent e) { handle(e); }
    }

    public static class MouseScrollingProxyNormal extends MouseScrollingProxy {
        static final MouseScrollingProxyNormal INSTANCE = new MouseScrollingProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(InputEvent.MouseScrollingEvent e) { handle(e); }
    }

    public static class MouseScrollingProxyLow extends MouseScrollingProxy {
        static final MouseScrollingProxyLow INSTANCE = new MouseScrollingProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(InputEvent.MouseScrollingEvent e) { handle(e); }
    }

    public static class MouseScrollingProxyLowest extends MouseScrollingProxy {
        static final MouseScrollingProxyLowest INSTANCE = new MouseScrollingProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(InputEvent.MouseScrollingEvent e) { handle(e); }
    }
}
