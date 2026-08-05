package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgePrepareClientTickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PrepareClientTickEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareClientTickProxyHighest.INSTANCE;
            case HIGH -> PrepareClientTickProxyHigh.INSTANCE;
            case NORMAL -> PrepareClientTickProxyNormal.INSTANCE;
            case LOW -> PrepareClientTickProxyLow.INSTANCE;
            case LOWEST -> PrepareClientTickProxyLowest.INSTANCE;
        };
    }

    private static abstract class ClientTickProxy extends AbstractEventCommon {
        public ClientTickProxy() {
            super(EventType.PREPARE_CLIENT_TICK_EVENT);
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
            return new ForgePrepareClientTickEvent(event);
        }

        protected void handle(TickEvent.ClientTickEvent.Pre event) {
            super.onEvent(event);
        }
    }

    public static class PrepareClientTickProxyHighest extends ClientTickProxy {
        static final PrepareClientTickProxyHighest INSTANCE = new PrepareClientTickProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.ClientTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientTickProxyHigh extends ClientTickProxy {
        static final PrepareClientTickProxyHigh INSTANCE = new PrepareClientTickProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.ClientTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientTickProxyNormal extends ClientTickProxy {
        static final PrepareClientTickProxyNormal INSTANCE = new PrepareClientTickProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.ClientTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientTickProxyLow extends ClientTickProxy {
        static final PrepareClientTickProxyLow INSTANCE = new PrepareClientTickProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.ClientTickEvent.Pre e) { handle(e); }
    }

    public static class PrepareClientTickProxyLowest extends ClientTickProxy {
        static final PrepareClientTickProxyLowest INSTANCE = new PrepareClientTickProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.ClientTickEvent.Pre e) { handle(e); }
    }
}