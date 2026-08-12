package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoComputeCameraAnglesEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoComputeCameraAnglesEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ComputeCameraAnglesProxyHighest.INSTANCE;
            case HIGH -> ComputeCameraAnglesProxyHigh.INSTANCE;
            case NORMAL -> ComputeCameraAnglesProxyNormal.INSTANCE;
            case LOW -> ComputeCameraAnglesProxyLow.INSTANCE;
            case LOWEST -> ComputeCameraAnglesProxyLowest.INSTANCE;
        };
    }

    private static abstract class ComputeCameraAnglesProxy extends AbstractNeoEventCommon {
        public ComputeCameraAnglesProxy() {
            super(EventType.COMPUTE_CAMERA_ANGLES_EVENT);
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
            return new NeoComputeCameraAnglesEvent(event);
        }

        protected void handle(ViewportEvent.ComputeCameraAngles event) {
            super.onEvent(event);
        }
    }

    public static class ComputeCameraAnglesProxyHighest extends ComputeCameraAnglesProxy {
        static final ComputeCameraAnglesProxyHighest INSTANCE = new ComputeCameraAnglesProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeCameraAngles e) { handle(e); }
    }

    public static class ComputeCameraAnglesProxyHigh extends ComputeCameraAnglesProxy {
        static final ComputeCameraAnglesProxyHigh INSTANCE = new ComputeCameraAnglesProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeCameraAngles e) { handle(e); }
    }

    public static class ComputeCameraAnglesProxyNormal extends ComputeCameraAnglesProxy {
        static final ComputeCameraAnglesProxyNormal INSTANCE = new ComputeCameraAnglesProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeCameraAngles e) { handle(e); }
    }

    public static class ComputeCameraAnglesProxyLow extends ComputeCameraAnglesProxy {
        static final ComputeCameraAnglesProxyLow INSTANCE = new ComputeCameraAnglesProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeCameraAngles e) { handle(e); }
    }

    public static class ComputeCameraAnglesProxyLowest extends ComputeCameraAnglesProxy {
        static final ComputeCameraAnglesProxyLowest INSTANCE = new ComputeCameraAnglesProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(ViewportEvent.ComputeCameraAngles e) { handle(e); }
    }
}