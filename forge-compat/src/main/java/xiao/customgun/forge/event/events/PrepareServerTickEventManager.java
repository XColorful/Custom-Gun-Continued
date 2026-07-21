package xiao.customgun.forge.event.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.ForgeServerTickEvent;

public class PrepareServerTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareServerTickProxyHighest.INSTANCE;
            case HIGH -> PrepareServerTickProxyHigh.INSTANCE;
            case NORMAL -> PrepareServerTickProxyNormal.INSTANCE;
            case LOW -> PrepareServerTickProxyLow.INSTANCE;
            case LOWEST -> PrepareServerTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ServerTickProxy extends AbstractEventCommon {
        public ServerTickProxy() {
            super(EventType.PREPARE_SERVER_TICK_EVENT);
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
            return new ForgeServerTickEvent(event);
        }

        protected void handle(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                super.onEvent(event);
            }
        }
    }

    public static class PrepareServerTickProxyHighest extends ServerTickProxy {
        static final PrepareServerTickProxyHighest INSTANCE = new PrepareServerTickProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class PrepareServerTickProxyHigh extends ServerTickProxy {
        static final PrepareServerTickProxyHigh INSTANCE = new PrepareServerTickProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class PrepareServerTickProxyNormal extends ServerTickProxy {
        static final PrepareServerTickProxyNormal INSTANCE = new PrepareServerTickProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class PrepareServerTickProxyLow extends ServerTickProxy {
        static final PrepareServerTickProxyLow INSTANCE = new PrepareServerTickProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }

    public static class PrepareServerTickProxyLowest extends ServerTickProxy {
        static final PrepareServerTickProxyLowest INSTANCE = new PrepareServerTickProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.ServerTickEvent e) { handle(e); }
    }
}
