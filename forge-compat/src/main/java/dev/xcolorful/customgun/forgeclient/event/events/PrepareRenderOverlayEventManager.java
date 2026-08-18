package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgePrepareRenderOverlayEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PrepareRenderOverlayEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareRenderOverlayProxyHighest.INSTANCE;
            case HIGH -> PrepareRenderOverlayProxyHigh.INSTANCE;
            case NORMAL -> PrepareRenderOverlayProxyNormal.INSTANCE;
            case LOW -> PrepareRenderOverlayProxyLow.INSTANCE;
            case LOWEST -> PrepareRenderOverlayProxyLowest.INSTANCE;
        };
    }

    private static abstract class PrepareRenderOverlayProxy extends AbstractEventCommon {
        public PrepareRenderOverlayProxy() {
            super(EventType.PREPARE_RENDER_OVERLAY_EVENT);
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
            return new ForgePrepareRenderOverlayEvent(event);
        }

        protected void handle(RenderGuiEvent.Pre event) {
            super.onEvent(event);
        }
    }

    public static class PrepareRenderOverlayProxyHighest extends PrepareRenderOverlayProxy {
        static final PrepareRenderOverlayProxyHighest INSTANCE = new PrepareRenderOverlayProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderOverlayProxyHigh extends PrepareRenderOverlayProxy {
        static final PrepareRenderOverlayProxyHigh INSTANCE = new PrepareRenderOverlayProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderOverlayProxyNormal extends PrepareRenderOverlayProxy {
        static final PrepareRenderOverlayProxyNormal INSTANCE = new PrepareRenderOverlayProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderOverlayProxyLow extends PrepareRenderOverlayProxy {
        static final PrepareRenderOverlayProxyLow INSTANCE = new PrepareRenderOverlayProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderOverlayProxyLowest extends PrepareRenderOverlayProxy {
        static final PrepareRenderOverlayProxyLowest INSTANCE = new PrepareRenderOverlayProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }
}