package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoPrepareClientPlayerTickEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class NeoPrepareClientPlayerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareClientPlayerTickProxyHighest.INSTANCE;
            case HIGH -> PrepareClientPlayerTickProxyHigh.INSTANCE;
            case NORMAL -> PrepareClientPlayerTickProxyNormal.INSTANCE;
            case LOW -> PrepareClientPlayerTickProxyLow.INSTANCE;
            case LOWEST -> PrepareClientPlayerTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ClientPlayerTickProxy extends AbstractNeoEventCommon {
        public ClientPlayerTickProxy() {
            super(EventType.PREPARE_CLIENT_PLAYER_TICK_EVENT);
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
            return new NeoPrepareClientPlayerTickEvent(event);
        }

        protected void handle(PlayerTickEvent.Pre event) {
            if (event.getEntity().level().isClientSide()) {
                super.onEvent(event);
            }
        }
    }

    public static class PrepareClientPlayerTickProxyHighest extends ClientPlayerTickProxy {
        static final PrepareClientPlayerTickProxyHighest INSTANCE = new PrepareClientPlayerTickProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(PlayerTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientPlayerTickProxyHigh extends ClientPlayerTickProxy {
        static final PrepareClientPlayerTickProxyHigh INSTANCE = new PrepareClientPlayerTickProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(PlayerTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientPlayerTickProxyNormal extends ClientPlayerTickProxy {
        static final PrepareClientPlayerTickProxyNormal INSTANCE = new PrepareClientPlayerTickProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(PlayerTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientPlayerTickProxyLow extends ClientPlayerTickProxy {
        static final PrepareClientPlayerTickProxyLow INSTANCE = new PrepareClientPlayerTickProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(PlayerTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientPlayerTickProxyLowest extends ClientPlayerTickProxy {
        static final PrepareClientPlayerTickProxyLowest INSTANCE = new PrepareClientPlayerTickProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(PlayerTickEvent.Pre e) { handle(e); }
    }
}
