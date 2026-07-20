package xiao.customgun.forge.event.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.common.McSideHelper;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.ForgePrepareServerPlayerTickEvent;

public class PrepareServerPlayerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ServerPlayerTickProxyHighest.INSTANCE;
            case HIGH -> ServerPlayerTickProxyHigh.INSTANCE;
            case NORMAL -> ServerPlayerTickProxyNormal.INSTANCE;
            case LOW -> ServerPlayerTickProxyLow.INSTANCE;
            case LOWEST -> ServerPlayerTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ServerPlayerTickProxy extends AbstractEventCommon {
        public ServerPlayerTickProxy() {
            super(EventType.PREPARE_SERVER_PLAYER_TICK_EVENT);
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
            return new ForgePrepareServerPlayerTickEvent(event);
        }

        protected void handle(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.START) {
                return;
            }
            if (event.side.isServer()) {
                super.onEvent(event);
            }
        }
    }

    public static class ServerPlayerTickProxyHighest extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyHighest INSTANCE = new ServerPlayerTickProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ServerPlayerTickProxyHigh extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyHigh INSTANCE = new ServerPlayerTickProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ServerPlayerTickProxyNormal extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyNormal INSTANCE = new ServerPlayerTickProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ServerPlayerTickProxyLow extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyLow INSTANCE = new ServerPlayerTickProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class ServerPlayerTickProxyLowest extends ServerPlayerTickProxy {
        static final ServerPlayerTickProxyLowest INSTANCE = new ServerPlayerTickProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }
}
