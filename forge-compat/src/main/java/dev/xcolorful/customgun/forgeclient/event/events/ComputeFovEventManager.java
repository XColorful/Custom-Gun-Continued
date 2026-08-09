package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgeComputeFovEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ComputeFovEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ComputeFovProxyHighest.INSTANCE;
            case HIGH -> ComputeFovProxyHigh.INSTANCE;
            case NORMAL -> ComputeFovProxyNormal.INSTANCE;
            case LOW -> ComputeFovProxyLow.INSTANCE;
            case LOWEST -> ComputeFovProxyLowest.INSTANCE;
        };
    }

    private static abstract class ComputeFovProxy extends AbstractEventCommon {
        public ComputeFovProxy() {
            super(EventType.COMPUTE_CAMERA_ANGLES_EVENT);
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
            return new ForgeComputeFovEvent(event);
        }

        protected void handle(ViewportEvent.ComputeFov event) {
            super.onEvent(event);
        }
    }

    public static class ComputeFovProxyHighest extends ComputeFovProxy {
        static final ComputeFovProxyHighest INSTANCE = new ComputeFovProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeFov e) { handle(e); }
    }

    public static class ComputeFovProxyHigh extends ComputeFovProxy {
        static final ComputeFovProxyHigh INSTANCE = new ComputeFovProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeFov e) { handle(e); }
    }

    public static class ComputeFovProxyNormal extends ComputeFovProxy {
        static final ComputeFovProxyNormal INSTANCE = new ComputeFovProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeFov e) { handle(e); }
    }

    public static class ComputeFovProxyLow extends ComputeFovProxy {
        static final ComputeFovProxyLow INSTANCE = new ComputeFovProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeFov e) { handle(e); }
    }

    public static class ComputeFovProxyLowest extends ComputeFovProxy {
        static final ComputeFovProxyLowest INSTANCE = new ComputeFovProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeFov e) { handle(e); }
    }
}