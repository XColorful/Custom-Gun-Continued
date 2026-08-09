package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgeComputeCameraAnglesEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ComputeCameraAnglesEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ComputeCameraAnglesProxyHighest.INSTANCE;
            case HIGH -> ComputeCameraAnglesProxyHigh.INSTANCE;
            case NORMAL -> ComputeCameraAnglesProxyNormal.INSTANCE;
            case LOW -> ComputeCameraAnglesProxyLow.INSTANCE;
            case LOWEST -> ComputeCameraAnglesProxyLowest.INSTANCE;
        };
    }

    private static abstract class ComputeCameraAnglesProxy extends AbstractEventCommon {
        public ComputeCameraAnglesProxy() {
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
            return new ForgeComputeCameraAnglesEvent(event);
        }

        protected void handle(ViewportEvent.ComputeCameraAngles event) {
            super.onEvent(event);
        }
    }

    public static class ComputeCameraAnglesProxyHighest extends ComputeCameraAnglesProxy {
        static final ComputeCameraAnglesProxyHighest INSTANCE = new ComputeCameraAnglesProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeCameraAngles e) { handle(e); }
    }

    public static class ComputeCameraAnglesProxyHigh extends ComputeCameraAnglesProxy {
        static final ComputeCameraAnglesProxyHigh INSTANCE = new ComputeCameraAnglesProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeCameraAngles e) { handle(e); }
    }

    public static class ComputeCameraAnglesProxyNormal extends ComputeCameraAnglesProxy {
        static final ComputeCameraAnglesProxyNormal INSTANCE = new ComputeCameraAnglesProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeCameraAngles e) { handle(e); }
    }

    public static class ComputeCameraAnglesProxyLow extends ComputeCameraAnglesProxy {
        static final ComputeCameraAnglesProxyLow INSTANCE = new ComputeCameraAnglesProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeCameraAngles e) { handle(e); }
    }

    public static class ComputeCameraAnglesProxyLowest extends ComputeCameraAnglesProxy {
        static final ComputeCameraAnglesProxyLowest INSTANCE = new ComputeCameraAnglesProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeCameraAngles e) { handle(e); }
    }
}