package dev.xcolorful.customgun.forge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.forge.event.ForgeEvent;
import dev.xcolorful.customgun.forge.event.ForgeTagsUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TagsUpdatedEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> TagsUpdatedProxyHighest.INSTANCE;
            case HIGH -> TagsUpdatedProxyHigh.INSTANCE;
            case NORMAL -> TagsUpdatedProxyNormal.INSTANCE;
            case LOW -> TagsUpdatedProxyLow.INSTANCE;
            case LOWEST -> TagsUpdatedProxyLowest.INSTANCE;
        };
    }

    private static abstract class TagsUpdatedProxy extends AbstractEventCommon {
        public TagsUpdatedProxy() {
            super(EventType.TAGS_UPDATED_EVENT);
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
            return new ForgeTagsUpdatedEvent(event);
        }

        protected void handle(TagsUpdatedEvent event) {
            super.onEvent(event);
        }
    }

    public static class TagsUpdatedProxyHighest extends TagsUpdatedProxy {
        static final TagsUpdatedProxyHighest INSTANCE = new TagsUpdatedProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TagsUpdatedEvent e) { handle(e); }
    }

    public static class TagsUpdatedProxyHigh extends TagsUpdatedProxy {
        static final TagsUpdatedProxyHigh INSTANCE = new TagsUpdatedProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TagsUpdatedEvent e) { handle(e); }
    }

    public static class TagsUpdatedProxyNormal extends TagsUpdatedProxy {
        static final TagsUpdatedProxyNormal INSTANCE = new TagsUpdatedProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TagsUpdatedEvent e) { handle(e); }
    }

    public static class TagsUpdatedProxyLow extends TagsUpdatedProxy {
        static final TagsUpdatedProxyLow INSTANCE = new TagsUpdatedProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TagsUpdatedEvent e) { handle(e); }
    }

    public static class TagsUpdatedProxyLowest extends TagsUpdatedProxy {
        static final TagsUpdatedProxyLowest INSTANCE = new TagsUpdatedProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TagsUpdatedEvent e) { handle(e); }
    }
}