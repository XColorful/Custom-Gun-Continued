package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoRenderOverlayEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoRenderOverlayEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RenderOverlayProxyHighest.INSTANCE;
            case HIGH -> RenderOverlayProxyHigh.INSTANCE;
            case NORMAL -> RenderOverlayProxyNormal.INSTANCE;
            case LOW -> RenderOverlayProxyLow.INSTANCE;
            case LOWEST -> RenderOverlayProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderOverlayProxy extends AbstractNeoEventCommon {
        public RenderOverlayProxy() {
            super(EventType.RENDER_OVERLAY_EVENT);
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
            return new NeoRenderOverlayEvent(event);
        }

        protected void handle(RenderGuiEvent.Post event) {
            super.onEvent(event);
        }
    }

    public static class RenderOverlayProxyHighest extends RenderOverlayProxy {
        static final RenderOverlayProxyHighest INSTANCE = new RenderOverlayProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderOverlayProxyHigh extends RenderOverlayProxy {
        static final RenderOverlayProxyHigh INSTANCE = new RenderOverlayProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderOverlayProxyNormal extends RenderOverlayProxy {
        static final RenderOverlayProxyNormal INSTANCE = new RenderOverlayProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderOverlayProxyLow extends RenderOverlayProxy {
        static final RenderOverlayProxyLow INSTANCE = new RenderOverlayProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderOverlayProxyLowest extends RenderOverlayProxy {
        static final RenderOverlayProxyLowest INSTANCE = new RenderOverlayProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }
}