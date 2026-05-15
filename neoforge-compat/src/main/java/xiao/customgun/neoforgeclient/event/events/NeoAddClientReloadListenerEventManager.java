package xiao.customgun.neoforgeclient.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.CustomGunNeoforge;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.events.AbstractNeoEventCommon;
import xiao.customgun.neoforgeclient.event.NeoAddClientReloadListenerEvent;

public class NeoAddClientReloadListenerEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> AddClientReloadListenerProxyHighest.INSTANCE;
            case HIGH -> AddClientReloadListenerProxyHigh.INSTANCE;
            case NORMAL -> AddClientReloadListenerProxyNormal.INSTANCE;
            case LOW -> AddClientReloadListenerProxyLow.INSTANCE;
            case LOWEST -> AddClientReloadListenerProxyLowest.INSTANCE;
        };
    }

    private static abstract class AddClientReloadListenerProxy extends AbstractNeoEventCommon {
        public AddClientReloadListenerProxy() {
            super(EventType.ADD_CLIENT_RELOAD_LISTENER_EVENT);
        }

        @Override
        protected void registerToNeo() {
            CustomGunNeoforge.modContainer.getEventBus().register(this);
        }

        @Override
        protected void unregisterToNeo() {
            CustomGunNeoforge.modContainer.getEventBus().unregister(this);
        }

        @Override
        protected NeoEvent getNeoEventType(Event event) {
            return new NeoAddClientReloadListenerEvent(event);
        }

        protected void handle(AddClientReloadListenersEvent event) {
            super.onEvent(event);
        }
    }

    public static class AddClientReloadListenerProxyHighest extends AddClientReloadListenerProxy {
        static final AddClientReloadListenerProxyHighest INSTANCE = new AddClientReloadListenerProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(AddClientReloadListenersEvent e) { handle(e); }
    }

    public static class AddClientReloadListenerProxyHigh extends AddClientReloadListenerProxy {
        static final AddClientReloadListenerProxyHigh INSTANCE = new AddClientReloadListenerProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(AddClientReloadListenersEvent e) { handle(e); }
    }

    public static class AddClientReloadListenerProxyNormal extends AddClientReloadListenerProxy {
        static final AddClientReloadListenerProxyNormal INSTANCE = new AddClientReloadListenerProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(AddClientReloadListenersEvent e) { handle(e); }
    }

    public static class AddClientReloadListenerProxyLow extends AddClientReloadListenerProxy {
        static final AddClientReloadListenerProxyLow INSTANCE = new AddClientReloadListenerProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(AddClientReloadListenersEvent e) { handle(e); }
    }

    public static class AddClientReloadListenerProxyLowest extends AddClientReloadListenerProxy {
        static final AddClientReloadListenerProxyLowest INSTANCE = new AddClientReloadListenerProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(AddClientReloadListenersEvent e) { handle(e); }
    }
}