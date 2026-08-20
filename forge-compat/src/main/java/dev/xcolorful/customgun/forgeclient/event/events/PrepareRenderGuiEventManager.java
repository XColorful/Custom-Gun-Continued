package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgePrepareRenderGuiEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PrepareRenderGuiEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PrepareRenderGuiProxyHighest.INSTANCE;
            case HIGH -> PrepareRenderGuiProxyHigh.INSTANCE;
            case NORMAL -> PrepareRenderGuiProxyNormal.INSTANCE;
            case LOW -> PrepareRenderGuiProxyLow.INSTANCE;
            case LOWEST -> PrepareRenderGuiProxyLowest.INSTANCE;
        };
    }

    private static abstract class PrepareRenderGuiProxy extends AbstractEventCommon {
        public PrepareRenderGuiProxy() {
            super(EventType.PREPARE_RENDER_GUI_EVENT);
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
            return new ForgePrepareRenderGuiEvent(event);
        }

        protected void handle(RenderGuiEvent.Pre event) {
            super.onEvent(event);
        }
    }

    public static class PrepareRenderGuiProxyHighest extends PrepareRenderGuiProxy {
        static final PrepareRenderGuiProxyHighest INSTANCE = new PrepareRenderGuiProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderGuiProxyHigh extends PrepareRenderGuiProxy {
        static final PrepareRenderGuiProxyHigh INSTANCE = new PrepareRenderGuiProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderGuiProxyNormal extends PrepareRenderGuiProxy {
        static final PrepareRenderGuiProxyNormal INSTANCE = new PrepareRenderGuiProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderGuiProxyLow extends PrepareRenderGuiProxy {
        static final PrepareRenderGuiProxyLow INSTANCE = new PrepareRenderGuiProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }

    public static class PrepareRenderGuiProxyLowest extends PrepareRenderGuiProxy {
        static final PrepareRenderGuiProxyLowest INSTANCE = new PrepareRenderGuiProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(RenderGuiEvent.Pre e) { handle(e); }
    }
}