package dev.xcolorful.customgun.forge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeAddServerReloadListenerEvent;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AddServerReloadListenerEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> AddServerReloadListenerProxyHighest.INSTANCE;
            case HIGH -> AddServerReloadListenerProxyHigh.INSTANCE;
            case NORMAL -> AddServerReloadListenerProxyNormal.INSTANCE;
            case LOW -> AddServerReloadListenerProxyLow.INSTANCE;
            case LOWEST -> AddServerReloadListenerProxyLowest.INSTANCE;
        };
    }

    private static abstract class AddServerReloadListenerProxy extends AbstractEventCommon {
        public AddServerReloadListenerProxy() {
            super(EventType.ADD_SERVER_RELOAD_LISTENER_EVENT);
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
            return new ForgeAddServerReloadListenerEvent(event);
        }

        protected void handle(AddReloadListenerEvent event) {
            super.onEvent(event);
        }
    }

    public static class AddServerReloadListenerProxyHighest extends AddServerReloadListenerProxy {
        static final AddServerReloadListenerProxyHighest INSTANCE = new AddServerReloadListenerProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(AddReloadListenerEvent e) { handle(e); }
    }

    public static class AddServerReloadListenerProxyHigh extends AddServerReloadListenerProxy {
        static final AddServerReloadListenerProxyHigh INSTANCE = new AddServerReloadListenerProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(AddReloadListenerEvent e) { handle(e); }
    }

    public static class AddServerReloadListenerProxyNormal extends AddServerReloadListenerProxy {
        static final AddServerReloadListenerProxyNormal INSTANCE = new AddServerReloadListenerProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(AddReloadListenerEvent e) { handle(e); }
    }

    public static class AddServerReloadListenerProxyLow extends AddServerReloadListenerProxy {
        static final AddServerReloadListenerProxyLow INSTANCE = new AddServerReloadListenerProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(AddReloadListenerEvent e) { handle(e); }
    }

    public static class AddServerReloadListenerProxyLowest extends AddServerReloadListenerProxy {
        static final AddServerReloadListenerProxyLowest INSTANCE = new AddServerReloadListenerProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(AddReloadListenerEvent e) { handle(e); }
    }
}