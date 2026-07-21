package xiao.customgun.forgeclient.event.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.common.McSideHelper;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.events.AbstractEventCommon;
import xiao.customgun.forgeclient.event.ForgeClientPlayerTickEvent;

public class ClientPlayerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ClientPlayerTickProxyHighest.INSTANCE;
            case HIGH -> ClientPlayerTickProxyHigh.INSTANCE;
            case NORMAL -> ClientPlayerTickProxyNormal.INSTANCE;
            case LOW -> ClientPlayerTickProxyLow.INSTANCE;
            case LOWEST -> ClientPlayerTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ClientPlayerTickProxy extends AbstractEventCommon {
        public ClientPlayerTickProxy() {
            super(EventType.CLIENT_PLAYER_TICK_EVENT);
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
            return new ForgeClientPlayerTickEvent(event);
        }

        protected void handle(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }
            if (event.side.isClient()) {
                super.onEvent(event);
            }
        }
    }

    public static class ClientPlayerTickProxyHighest extends ClientPlayerTickProxy {
        static final ClientPlayerTickProxyHighest INSTANCE = new ClientPlayerTickProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ClientPlayerTickProxyHigh extends ClientPlayerTickProxy {
        static final ClientPlayerTickProxyHigh INSTANCE = new ClientPlayerTickProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ClientPlayerTickProxyNormal extends ClientPlayerTickProxy {
        static final ClientPlayerTickProxyNormal INSTANCE = new ClientPlayerTickProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ClientPlayerTickProxyLow extends ClientPlayerTickProxy {
        static final ClientPlayerTickProxyLow INSTANCE = new ClientPlayerTickProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ClientPlayerTickProxyLowest extends ClientPlayerTickProxy {
        static final ClientPlayerTickProxyLowest INSTANCE = new ClientPlayerTickProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }
}
