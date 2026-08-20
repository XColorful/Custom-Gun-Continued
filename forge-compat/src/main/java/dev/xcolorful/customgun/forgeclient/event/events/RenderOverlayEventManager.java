package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgeRenderOverlayEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderOverlayEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RenderOverlayProxyHighest.INSTANCE;
            case HIGH -> RenderOverlayProxyHigh.INSTANCE;
            case NORMAL -> RenderOverlayProxyNormal.INSTANCE;
            case LOW -> RenderOverlayProxyLow.INSTANCE;
            case LOWEST -> RenderOverlayProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderOverlayProxy extends AbstractEventCommon {
        public RenderOverlayProxy() {
            super(EventType.RENDER_OVERLAY_EVENT);
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
            return new ForgeRenderOverlayEvent(event);
        }

        protected void handle(RenderGuiEvent.Post event) {
            super.onEvent(event);
        }
    }

    public static class RenderOverlayProxyHighest extends RenderOverlayProxy {
        static final RenderOverlayProxyHighest INSTANCE = new RenderOverlayProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderOverlayProxyHigh extends RenderOverlayProxy {
        static final RenderOverlayProxyHigh INSTANCE = new RenderOverlayProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderOverlayProxyNormal extends RenderOverlayProxy {
        static final RenderOverlayProxyNormal INSTANCE = new RenderOverlayProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderOverlayProxyLow extends RenderOverlayProxy {
        static final RenderOverlayProxyLow INSTANCE = new RenderOverlayProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }

    public static class RenderOverlayProxyLowest extends RenderOverlayProxy {
        static final RenderOverlayProxyLowest INSTANCE = new RenderOverlayProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Post e) { handle(e); }
    }
}