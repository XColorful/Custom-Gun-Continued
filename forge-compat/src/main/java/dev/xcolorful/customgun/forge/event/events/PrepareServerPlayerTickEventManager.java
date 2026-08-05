package dev.xcolorful.customgun.forge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.ForgePrepareServerPlayerTickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PrepareServerPlayerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareServerPlayerTickProxyHighest.INSTANCE;
            case HIGH -> PrepareServerPlayerTickProxyHigh.INSTANCE;
            case NORMAL -> PrepareServerPlayerTickProxyNormal.INSTANCE;
            case LOW -> PrepareServerPlayerTickProxyLow.INSTANCE;
            case LOWEST -> PrepareServerPlayerTickProxyLowest.INSTANCE;
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

    public static class PrepareServerPlayerTickProxyHighest extends ServerPlayerTickProxy {
        static final PrepareServerPlayerTickProxyHighest INSTANCE = new PrepareServerPlayerTickProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class PrepareServerPlayerTickProxyHigh extends ServerPlayerTickProxy {
        static final PrepareServerPlayerTickProxyHigh INSTANCE = new PrepareServerPlayerTickProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class PrepareServerPlayerTickProxyNormal extends ServerPlayerTickProxy {
        static final PrepareServerPlayerTickProxyNormal INSTANCE = new PrepareServerPlayerTickProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class PrepareServerPlayerTickProxyLow extends ServerPlayerTickProxy {
        static final PrepareServerPlayerTickProxyLow INSTANCE = new PrepareServerPlayerTickProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }

    public static class PrepareServerPlayerTickProxyLowest extends ServerPlayerTickProxy {
        static final PrepareServerPlayerTickProxyLowest INSTANCE = new PrepareServerPlayerTickProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.PlayerTickEvent e) { handle(e); }
    }
}
