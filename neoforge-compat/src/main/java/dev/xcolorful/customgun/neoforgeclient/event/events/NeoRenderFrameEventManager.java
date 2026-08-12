package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoRenderFrameEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoRenderFrameEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RenderFrameProxyHighest.INSTANCE;
            case HIGH -> RenderFrameProxyHigh.INSTANCE;
            case NORMAL -> RenderFrameProxyNormal.INSTANCE;
            case LOW -> RenderFrameProxyLow.INSTANCE;
            case LOWEST -> RenderFrameProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderFrameProxy extends AbstractNeoEventCommon {
        public RenderFrameProxy() {
            super(EventType.RENDER_FRAME_EVENT);
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
            return new NeoRenderFrameEvent(event);
        }

        protected void handle(RenderFrameEvent.Post event) {
            super.onEvent(event);
        }
    }

    public static class RenderFrameProxyHighest extends RenderFrameProxy {
        static final RenderFrameProxyHighest INSTANCE = new RenderFrameProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderFrameEvent.Post e) { handle(e); }
    }

    public static class RenderFrameProxyHigh extends RenderFrameProxy {
        static final RenderFrameProxyHigh INSTANCE = new RenderFrameProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderFrameEvent.Post e) { handle(e); }
    }

    public static class RenderFrameProxyNormal extends RenderFrameProxy {
        static final RenderFrameProxyNormal INSTANCE = new RenderFrameProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderFrameEvent.Post e) { handle(e); }
    }

    public static class RenderFrameProxyLow extends RenderFrameProxy {
        static final RenderFrameProxyLow INSTANCE = new RenderFrameProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderFrameEvent.Post e) { handle(e); }
    }

    public static class RenderFrameProxyLowest extends RenderFrameProxy {
        static final RenderFrameProxyLowest INSTANCE = new RenderFrameProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderFrameEvent.Post e) { handle(e); }
    }
}