package xiao.customgun.neoforgeclient.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.events.AbstractNeoEventCommon;
import xiao.customgun.neoforgeclient.event.NeoPrepareClientTickEvent;

public class NeoPrepareClientTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareClientTickProxyHighest.INSTANCE;
            case HIGH -> PrepareClientTickProxyHigh.INSTANCE;
            case NORMAL -> PrepareClientTickProxyNormal.INSTANCE;
            case LOW -> PrepareClientTickProxyLow.INSTANCE;
            case LOWEST -> PrepareClientTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ClientTickProxy extends AbstractNeoEventCommon {
        public ClientTickProxy() {
            super(EventType.PREPARE_CLIENT_TICK_EVENT);
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
            return new NeoPrepareClientTickEvent(event);
        }

        protected void handle(ClientTickEvent.Pre event) {
            super.onEvent(event);
        }
    }

    public static class PrepareClientTickProxyHighest extends ClientTickProxy {
        static final PrepareClientTickProxyHighest INSTANCE = new PrepareClientTickProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(ClientTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientTickProxyHigh extends ClientTickProxy {
        static final PrepareClientTickProxyHigh INSTANCE = new PrepareClientTickProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(ClientTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientTickProxyNormal extends ClientTickProxy {
        static final PrepareClientTickProxyNormal INSTANCE = new PrepareClientTickProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(ClientTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientTickProxyLow extends ClientTickProxy {
        static final PrepareClientTickProxyLow INSTANCE = new PrepareClientTickProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(ClientTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientTickProxyLowest extends ClientTickProxy {
        static final PrepareClientTickProxyLowest INSTANCE = new PrepareClientTickProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(ClientTickEvent.Pre e) { handle(e); }
    }
}
