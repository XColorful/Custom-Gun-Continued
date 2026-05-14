package xiao.customgun.forgeclient.event.events;

import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.events.AbstractEventCommon;
import xiao.customgun.forgeclient.event.ForgeAddClientReloadListenerEvent;

public class AddClientReloadListenerEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> AddClientReloadListenerProxyHighest.INSTANCE;
            case HIGH -> AddClientReloadListenerProxyHigh.INSTANCE;
            case NORMAL -> AddClientReloadListenerProxyNormal.INSTANCE;
            case LOW -> AddClientReloadListenerProxyLow.INSTANCE;
            case LOWEST -> AddClientReloadListenerProxyLowest.INSTANCE;
        };
    }

    private static abstract class AddClientReloadListenerProxy extends AbstractEventCommon {
        public AddClientReloadListenerProxy() {
            super(EventType.ADD_CLIENT_RELOAD_LISTENER_EVENT);
        }

        @Override
        protected void registerToForge() {
            FMLJavaModLoadingContext.get().getModEventBus().register(this);
        }

        @Override
        protected void unregisterToForge() {
            FMLJavaModLoadingContext.get().getModEventBus().unregister(this);
        }

        @Override
        protected ForgeEvent getForgeEventType(Event event) {
            return new ForgeAddClientReloadListenerEvent(event);
        }

        protected void handle(RegisterClientReloadListenersEvent event) {
            super.onEvent(event);
        }
    }

    public static class AddClientReloadListenerProxyHighest extends AddClientReloadListenerProxy {
        static final AddClientReloadListenerProxyHighest INSTANCE = new AddClientReloadListenerProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RegisterClientReloadListenersEvent e) { handle(e); }
    }

    public static class AddClientReloadListenerProxyHigh extends AddClientReloadListenerProxy {
        static final AddClientReloadListenerProxyHigh INSTANCE = new AddClientReloadListenerProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RegisterClientReloadListenersEvent e) { handle(e); }
    }

    public static class AddClientReloadListenerProxyNormal extends AddClientReloadListenerProxy {
        static final AddClientReloadListenerProxyNormal INSTANCE = new AddClientReloadListenerProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RegisterClientReloadListenersEvent e) { handle(e); }
    }

    public static class AddClientReloadListenerProxyLow extends AddClientReloadListenerProxy {
        static final AddClientReloadListenerProxyLow INSTANCE = new AddClientReloadListenerProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RegisterClientReloadListenersEvent e) { handle(e); }
    }

    public static class AddClientReloadListenerProxyLowest extends AddClientReloadListenerProxy {
        static final AddClientReloadListenerProxyLowest INSTANCE = new AddClientReloadListenerProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RegisterClientReloadListenersEvent e) { handle(e); }
    }
}