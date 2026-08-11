package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgeRenderHandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderHandEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> RenderHandProxyHighest.INSTANCE;
            case HIGH -> RenderHandProxyHigh.INSTANCE;
            case NORMAL -> RenderHandProxyNormal.INSTANCE;
            case LOW -> RenderHandProxyLow.INSTANCE;
            case LOWEST -> RenderHandProxyLowest.INSTANCE;
        };
    }

    private static abstract class RenderHandProxy extends AbstractEventCommon {
        public RenderHandProxy() {
            super(EventType.RENDER_HAND_EVENT);
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
            return new ForgeRenderHandEvent(event);
        }

        protected void handle(RenderHandEvent event) {
            super.onEvent(event);
        }
    }

    public static class RenderHandProxyHighest extends RenderHandProxy {
        static final RenderHandProxyHighest INSTANCE = new RenderHandProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderHandEvent e) { handle(e); }
    }

    public static class RenderHandProxyHigh extends RenderHandProxy {
        static final RenderHandProxyHigh INSTANCE = new RenderHandProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderHandEvent e) { handle(e); }
    }

    public static class RenderHandProxyNormal extends RenderHandProxy {
        static final RenderHandProxyNormal INSTANCE = new RenderHandProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderHandEvent e) { handle(e); }
    }

    public static class RenderHandProxyLow extends RenderHandProxy {
        static final RenderHandProxyLow INSTANCE = new RenderHandProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderHandEvent e) { handle(e); }
    }

    public static class RenderHandProxyLowest extends RenderHandProxy {
        static final RenderHandProxyLowest INSTANCE = new RenderHandProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderHandEvent e) { handle(e); }
    }
}