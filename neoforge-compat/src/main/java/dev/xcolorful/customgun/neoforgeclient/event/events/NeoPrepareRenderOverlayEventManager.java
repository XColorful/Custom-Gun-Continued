package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoPrepareRenderOverlayEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoPrepareRenderOverlayEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareRenderOverlayProxyHighest.INSTANCE;
            case HIGH -> PrepareRenderOverlayProxyHigh.INSTANCE;
            case NORMAL -> PrepareRenderOverlayProxyNormal.INSTANCE;
            case LOW -> PrepareRenderOverlayProxyLow.INSTANCE;
            case LOWEST -> PrepareRenderOverlayProxyLowest.INSTANCE;
        };
    }

    private static abstract class PrepareRenderOverlayProxy extends AbstractNeoEventCommon {
        public PrepareRenderOverlayProxy() {
            super(EventType.PREPARE_RENDER_OVERLAY_EVENT);
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
            return new NeoPrepareRenderOverlayEvent(event);
        }

        protected void handle(RenderGuiEvent.Pre event) {
            super.onEvent(event);
        }
    }

    public static class PrepareRenderOverlayProxyHighest extends PrepareRenderOverlayProxy {
        static final PrepareRenderOverlayProxyHighest INSTANCE = new PrepareRenderOverlayProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderOverlayProxyHigh extends PrepareRenderOverlayProxy {
        static final PrepareRenderOverlayProxyHigh INSTANCE = new PrepareRenderOverlayProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderOverlayProxyNormal extends PrepareRenderOverlayProxy {
        static final PrepareRenderOverlayProxyNormal INSTANCE = new PrepareRenderOverlayProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderOverlayProxyLow extends PrepareRenderOverlayProxy {
        static final PrepareRenderOverlayProxyLow INSTANCE = new PrepareRenderOverlayProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderOverlayProxyLowest extends PrepareRenderOverlayProxy {
        static final PrepareRenderOverlayProxyLowest INSTANCE = new PrepareRenderOverlayProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }
}