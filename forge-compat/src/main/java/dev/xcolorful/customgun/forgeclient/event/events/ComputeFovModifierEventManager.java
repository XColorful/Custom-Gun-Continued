package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgeComputeFovModifierEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ComputeFovModifierEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> ComputeFovModifierProxyHighest.INSTANCE;
            case HIGH -> ComputeFovModifierProxyHigh.INSTANCE;
            case NORMAL -> ComputeFovModifierProxyNormal.INSTANCE;
            case LOW -> ComputeFovModifierProxyLow.INSTANCE;
            case LOWEST -> ComputeFovModifierProxyLowest.INSTANCE;
        };
    }

    private static abstract class ComputeFovModifierProxy extends AbstractEventCommon {
        public ComputeFovModifierProxy() {
            super(EventType.COMPUTE_FOV_MODIFIER_EVENT);
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
            return new ForgeComputeFovModifierEvent(event);
        }

        protected void handle(ComputeFovModifierEvent event) {
            super.onEvent(event);
        }
    }

    public static class ComputeFovModifierProxyHighest extends ComputeFovModifierProxy {
        static final ComputeFovModifierProxyHighest INSTANCE = new ComputeFovModifierProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(ComputeFovModifierEvent e) { handle(e); }
    }

    public static class ComputeFovModifierProxyHigh extends ComputeFovModifierProxy {
        static final ComputeFovModifierProxyHigh INSTANCE = new ComputeFovModifierProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(ComputeFovModifierEvent e) { handle(e); }
    }

    public static class ComputeFovModifierProxyNormal extends ComputeFovModifierProxy {
        static final ComputeFovModifierProxyNormal INSTANCE = new ComputeFovModifierProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(ComputeFovModifierEvent e) { handle(e); }
    }

    public static class ComputeFovModifierProxyLow extends ComputeFovModifierProxy {
        static final ComputeFovModifierProxyLow INSTANCE = new ComputeFovModifierProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(ComputeFovModifierEvent e) { handle(e); }
    }

    public static class ComputeFovModifierProxyLowest extends ComputeFovModifierProxy {
        static final ComputeFovModifierProxyLowest INSTANCE = new ComputeFovModifierProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(ComputeFovModifierEvent e) { handle(e); }
    }
}