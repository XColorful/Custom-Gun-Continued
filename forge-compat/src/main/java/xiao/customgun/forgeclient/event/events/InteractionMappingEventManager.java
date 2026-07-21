package xiao.customgun.forgeclient.event.events;

import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.events.AbstractEventCommon;
import xiao.customgun.forgeclient.event.ForgeInteractionMappingEvent;

public class InteractionMappingEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> InteractionMappingProxyHighest.INSTANCE;
            case HIGH -> InteractionMappingProxyHigh.INSTANCE;
            case NORMAL -> InteractionMappingProxyNormal.INSTANCE;
            case LOW -> InteractionMappingProxyLow.INSTANCE;
            case LOWEST -> InteractionMappingProxyLowest.INSTANCE;
        };
    }

    private static abstract class InteractionMappingProxy extends AbstractEventCommon {
        public InteractionMappingProxy() {
            super(EventType.INTERACTION_MAPPING_EVENT);
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
            return new ForgeInteractionMappingEvent(event);
        }

        protected void handle(InputEvent.InteractionKeyMappingTriggered event) {
            super.onEvent(event);
        }
    }

    public static class InteractionMappingProxyHighest extends InteractionMappingProxy {
        static final InteractionMappingProxyHighest INSTANCE = new InteractionMappingProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(InputEvent.InteractionKeyMappingTriggered e) { handle(e); }
    }

    public static class InteractionMappingProxyHigh extends InteractionMappingProxy {
        static final InteractionMappingProxyHigh INSTANCE = new InteractionMappingProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(InputEvent.InteractionKeyMappingTriggered e) { handle(e); }
    }

    public static class InteractionMappingProxyNormal extends InteractionMappingProxy {
        static final InteractionMappingProxyNormal INSTANCE = new InteractionMappingProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(InputEvent.InteractionKeyMappingTriggered e) { handle(e); }
    }

    public static class InteractionMappingProxyLow extends InteractionMappingProxy {
        static final InteractionMappingProxyLow INSTANCE = new InteractionMappingProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(InputEvent.InteractionKeyMappingTriggered e) { handle(e); }
    }

    public static class InteractionMappingProxyLowest extends InteractionMappingProxy {
        static final InteractionMappingProxyLowest INSTANCE = new InteractionMappingProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(InputEvent.InteractionKeyMappingTriggered e) { handle(e); }
    }
}
