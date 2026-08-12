package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoPrepareRenderFrameEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoPrepareRenderFrameEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareRenderFrameProxyHighest.INSTANCE;
            case HIGH -> PrepareRenderFrameProxyHigh.INSTANCE;
            case NORMAL -> PrepareRenderFrameProxyNormal.INSTANCE;
            case LOW -> PrepareRenderFrameProxyLow.INSTANCE;
            case LOWEST -> PrepareRenderFrameProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderFrameProxy extends AbstractNeoEventCommon {
        public RenderFrameProxy() {
            super(EventType.PREPARE_RENDER_FRAME_EVENT);
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
            return new NeoPrepareRenderFrameEvent(event);
        }

        protected void handle(RenderFrameEvent.Pre event) {
            super.onEvent(event);
        }
    }

    public static class PrepareRenderFrameProxyHighest extends RenderFrameProxy {
        static final PrepareRenderFrameProxyHighest INSTANCE = new PrepareRenderFrameProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderFrameEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderFrameProxyHigh extends RenderFrameProxy {
        static final PrepareRenderFrameProxyHigh INSTANCE = new PrepareRenderFrameProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderFrameEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderFrameProxyNormal extends RenderFrameProxy {
        static final PrepareRenderFrameProxyNormal INSTANCE = new PrepareRenderFrameProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderFrameEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderFrameProxyLow extends RenderFrameProxy {
        static final PrepareRenderFrameProxyLow INSTANCE = new PrepareRenderFrameProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderFrameEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderFrameProxyLowest extends RenderFrameProxy {
        static final PrepareRenderFrameProxyLowest INSTANCE = new PrepareRenderFrameProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderFrameEvent.Pre e) { handle(e); }
    }
}