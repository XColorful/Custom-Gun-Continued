package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgePrepareRenderFrameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PrepareRenderFrameEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareRenderFrameProxyHighest.INSTANCE;
            case HIGH -> PrepareRenderFrameProxyHigh.INSTANCE;
            case NORMAL -> PrepareRenderFrameProxyNormal.INSTANCE;
            case LOW -> PrepareRenderFrameProxyLow.INSTANCE;
            case LOWEST -> PrepareRenderFrameProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderFrameProxy extends AbstractEventCommon {
        public RenderFrameProxy() {
            super(EventType.PREPARE_RENDER_FRAME_EVENT);
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
            return new ForgePrepareRenderFrameEvent(event);
        }

        protected void handle(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.START) {
                super.onEvent(event);
            }
        }
    }

    public static class PrepareRenderFrameProxyHighest extends RenderFrameProxy {
        static final PrepareRenderFrameProxyHighest INSTANCE = new PrepareRenderFrameProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.RenderTickEvent e) { handle(e); }
    }

    public static class PrepareRenderFrameProxyHigh extends RenderFrameProxy {
        static final PrepareRenderFrameProxyHigh INSTANCE = new PrepareRenderFrameProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.RenderTickEvent e) { handle(e); }
    }

    public static class PrepareRenderFrameProxyNormal extends RenderFrameProxy {
        static final PrepareRenderFrameProxyNormal INSTANCE = new PrepareRenderFrameProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.RenderTickEvent e) { handle(e); }
    }

    public static class PrepareRenderFrameProxyLow extends RenderFrameProxy {
        static final PrepareRenderFrameProxyLow INSTANCE = new PrepareRenderFrameProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.RenderTickEvent e) { handle(e); }
    }

    public static class PrepareRenderFrameProxyLowest extends RenderFrameProxy {
        static final PrepareRenderFrameProxyLowest INSTANCE = new PrepareRenderFrameProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.RenderTickEvent e) { handle(e); }
    }
}