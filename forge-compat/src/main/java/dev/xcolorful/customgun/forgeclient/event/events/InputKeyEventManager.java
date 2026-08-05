package dev.xcolorful.customgun.forgeclient.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.events.AbstractEventCommon;
import dev.xcolorful.customgun.forgeclient.event.ForgeInputKeyEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InputKeyEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> InputKeyProxyHighest.INSTANCE;
            case HIGH -> InputKeyProxyHigh.INSTANCE;
            case NORMAL -> InputKeyProxyNormal.INSTANCE;
            case LOW -> InputKeyProxyLow.INSTANCE;
            case LOWEST -> InputKeyProxyLowest.INSTANCE;
        };
    }

    private static abstract class InputKeyProxy extends AbstractEventCommon {
        public InputKeyProxy() {
            super(EventType.INPUT_KEY_EVENT);
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
            return new ForgeInputKeyEvent(event);
        }

        protected void handle(InputEvent.Key event) {
            super.onEvent(event);
        }
    }

    public static class InputKeyProxyHighest extends InputKeyProxy {
        static final InputKeyProxyHighest INSTANCE = new InputKeyProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(InputEvent.Key e) { handle(e); }
    }

    public static class InputKeyProxyHigh extends InputKeyProxy {
        static final InputKeyProxyHigh INSTANCE = new InputKeyProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(InputEvent.Key e) { handle(e); }
    }

    public static class InputKeyProxyNormal extends InputKeyProxy {
        static final InputKeyProxyNormal INSTANCE = new InputKeyProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(InputEvent.Key e) { handle(e); }
    }

    public static class InputKeyProxyLow extends InputKeyProxy {
        static final InputKeyProxyLow INSTANCE = new InputKeyProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(InputEvent.Key e) { handle(e); }
    }

    public static class InputKeyProxyLowest extends InputKeyProxy {
        static final InputKeyProxyLowest INSTANCE = new InputKeyProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(InputEvent.Key e) { handle(e); }
    }
}
