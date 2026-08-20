package dev.xcolorful.customgun.neoforgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.events.AbstractNeoEventCommon;
import dev.xcolorful.customgun.neoforgeclient.event.NeoPrepareRenderGuiEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

public class NeoPrepareRenderGuiEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareRenderGuiProxyHighest.INSTANCE;
            case HIGH -> PrepareRenderGuiProxyHigh.INSTANCE;
            case NORMAL -> PrepareRenderGuiProxyNormal.INSTANCE;
            case LOW -> PrepareRenderGuiProxyLow.INSTANCE;
            case LOWEST -> PrepareRenderGuiProxyLowest.INSTANCE;
        };
    }

    private static abstract class PrepareRenderGuiProxy extends AbstractNeoEventCommon {
        public PrepareRenderGuiProxy() {
            super(EventType.PREPARE_RENDER_GUI_EVENT);
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
            return new NeoPrepareRenderGuiEvent(event);
        }

        protected void handle(RenderGuiEvent.Pre event) {
            super.onEvent(event);
        }
    }

    public static class PrepareRenderGuiProxyHighest extends PrepareRenderGuiProxy {
        static final PrepareRenderGuiProxyHighest INSTANCE = new PrepareRenderGuiProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderGuiProxyHigh extends PrepareRenderGuiProxy {
        static final PrepareRenderGuiProxyHigh INSTANCE = new PrepareRenderGuiProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderGuiProxyNormal extends PrepareRenderGuiProxy {
        static final PrepareRenderGuiProxyNormal INSTANCE = new PrepareRenderGuiProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderGuiProxyLow extends PrepareRenderGuiProxy {
        static final PrepareRenderGuiProxyLow INSTANCE = new PrepareRenderGuiProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderGuiProxyLowest extends PrepareRenderGuiProxy {
        static final PrepareRenderGuiProxyLowest INSTANCE = new PrepareRenderGuiProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }
}