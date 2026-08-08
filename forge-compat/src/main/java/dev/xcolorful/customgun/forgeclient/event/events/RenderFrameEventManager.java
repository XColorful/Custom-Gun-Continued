package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgeRenderFrameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderFrameEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RenderFrameProxyHighest.INSTANCE;
            case HIGH -> RenderFrameProxyHigh.INSTANCE;
            case NORMAL -> RenderFrameProxyNormal.INSTANCE;
            case LOW -> RenderFrameProxyLow.INSTANCE;
            case LOWEST -> RenderFrameProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderFrameProxy extends AbstractEventCommon {
        public RenderFrameProxy() {
            super(EventType.RENDER_FRAME_EVENT);
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
            return new ForgeRenderFrameEvent(event);
        }

        protected void handle(TickEvent.RenderTickEvent event) {
            if (event.phase == TickEvent.Phase.END) {
                super.onEvent(event);
            }
        }
    }

    public static class RenderFrameProxyHighest extends RenderFrameProxy {
        static final RenderFrameProxyHighest INSTANCE = new RenderFrameProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TickEvent.RenderTickEvent e) { handle(e); }
    }

    public static class RenderFrameProxyHigh extends RenderFrameProxy {
        static final RenderFrameProxyHigh INSTANCE = new RenderFrameProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TickEvent.RenderTickEvent e) { handle(e); }
    }

    public static class RenderFrameProxyNormal extends RenderFrameProxy {
        static final RenderFrameProxyNormal INSTANCE = new RenderFrameProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TickEvent.RenderTickEvent e) { handle(e); }
    }

    public static class RenderFrameProxyLow extends RenderFrameProxy {
        static final RenderFrameProxyLow INSTANCE = new RenderFrameProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TickEvent.RenderTickEvent e) { handle(e); }
    }

    public static class RenderFrameProxyLowest extends RenderFrameProxy {
        static final RenderFrameProxyLowest INSTANCE = new RenderFrameProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TickEvent.RenderTickEvent e) { handle(e); }
    }
}