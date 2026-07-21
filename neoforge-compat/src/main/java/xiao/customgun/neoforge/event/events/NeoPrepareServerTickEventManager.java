package xiao.customgun.neoforge.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TickEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.NeoServerTickEvent;

public class NeoPrepareServerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareServerTickProxyHighest.INSTANCE;
            case HIGH -> PrepareServerTickProxyHigh.INSTANCE;
            case NORMAL -> PrepareServerTickProxyNormal.INSTANCE;
            case LOW -> PrepareServerTickProxyLow.INSTANCE;
            case LOWEST -> PrepareServerTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ServerTickProxy extends AbstractNeoEventCommon {
        public ServerTickProxy() {
            super(EventType.PREPARE_SERVER_TICK_EVENT);
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
            return new NeoServerTickEvent(event);
        }

        protected void handle(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                super.onEvent(event);
            }
        }
    }

    public static class PrepareServerTickProxyHighest extends ServerTickProxy {
        static final PrepareServerTickProxyHighest INSTANCE = new PrepareServerTickProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class PrepareServerTickProxyHigh extends ServerTickProxy {
        static final PrepareServerTickProxyHigh INSTANCE = new PrepareServerTickProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class PrepareServerTickProxyNormal extends ServerTickProxy {
        static final PrepareServerTickProxyNormal INSTANCE = new PrepareServerTickProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class PrepareServerTickProxyLow extends ServerTickProxy {
        static final PrepareServerTickProxyLow INSTANCE = new PrepareServerTickProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class PrepareServerTickProxyLowest extends ServerTickProxy {
        static final PrepareServerTickProxyLowest INSTANCE = new PrepareServerTickProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }
}
