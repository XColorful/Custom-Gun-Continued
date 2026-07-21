package xiao.customgun.forgeclient.event.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.events.AbstractEventCommon;
import xiao.customgun.forgeclient.event.ForgePrepareClientPlayerTickEvent;

public class PrepareClientPlayerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareClientPlayerTickProxyHighest.INSTANCE;
            case HIGH -> PrepareClientPlayerTickProxyHigh.INSTANCE;
            case NORMAL -> PrepareClientPlayerTickProxyNormal.INSTANCE;
            case LOW -> PrepareClientPlayerTickProxyLow.INSTANCE;
            case LOWEST -> PrepareClientPlayerTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ClientPlayerTickProxy extends AbstractEventCommon {
        public ClientPlayerTickProxy() {
            super(EventType.PREPARE_CLIENT_PLAYER_TICK_EVENT);
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
            return new ForgePrepareClientPlayerTickEvent(event);
        }

        protected void handle(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.START) {
                return;
            }
            if (event.side.isClient()) {
                super.onEvent(event);
            }
        }
    }

    public static class PrepareClientPlayerTickProxyHighest extends ClientPlayerTickProxy {
        static final PrepareClientPlayerTickProxyHighest INSTANCE = new PrepareClientPlayerTickProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class PrepareClientPlayerTickProxyHigh extends ClientPlayerTickProxy {
        static final PrepareClientPlayerTickProxyHigh INSTANCE = new PrepareClientPlayerTickProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class PrepareClientPlayerTickProxyNormal extends ClientPlayerTickProxy {
        static final PrepareClientPlayerTickProxyNormal INSTANCE = new PrepareClientPlayerTickProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class PrepareClientPlayerTickProxyLow extends ClientPlayerTickProxy {
        static final PrepareClientPlayerTickProxyLow INSTANCE = new PrepareClientPlayerTickProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class PrepareClientPlayerTickProxyLowest extends ClientPlayerTickProxy {
        static final PrepareClientPlayerTickProxyLowest INSTANCE = new PrepareClientPlayerTickProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }
}
