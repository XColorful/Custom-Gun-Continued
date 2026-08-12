package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoRenderHandEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoRenderHandEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RenderHandProxyHighest.INSTANCE;
            case HIGH -> RenderHandProxyHigh.INSTANCE;
            case NORMAL -> RenderHandProxyNormal.INSTANCE;
            case LOW -> RenderHandProxyLow.INSTANCE;
            case LOWEST -> RenderHandProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderHandProxy extends AbstractNeoEventCommon {
        public RenderHandProxy() {
            super(EventType.RENDER_HAND_EVENT);
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
            return new NeoRenderHandEvent(event);
        }

        protected void handle(RenderHandEvent event) {
            super.onEvent(event);
        }
    }

    public static class RenderHandProxyHighest extends RenderHandProxy {
        static final RenderHandProxyHighest INSTANCE = new RenderHandProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderHandEvent e) { handle(e); }
    }

    public static class RenderHandProxyHigh extends RenderHandProxy {
        static final RenderHandProxyHigh INSTANCE = new RenderHandProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderHandEvent e) { handle(e); }
    }

    public static class RenderHandProxyNormal extends RenderHandProxy {
        static final RenderHandProxyNormal INSTANCE = new RenderHandProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderHandEvent e) { handle(e); }
    }

    public static class RenderHandProxyLow extends RenderHandProxy {
        static final RenderHandProxyLow INSTANCE = new RenderHandProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderHandEvent e) { handle(e); }
    }

    public static class RenderHandProxyLowest extends RenderHandProxy {
        static final RenderHandProxyLowest INSTANCE = new RenderHandProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderHandEvent e) { handle(e); }
    }
}