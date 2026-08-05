package dev.xcolorful.customgun.neoforge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.NeoTagsUpdatedEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

public class NeoTagsUpdatedEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> TagsUpdatedProxyHighest.INSTANCE;
            case HIGH -> TagsUpdatedProxyHigh.INSTANCE;
            case NORMAL -> TagsUpdatedProxyNormal.INSTANCE;
            case LOW -> TagsUpdatedProxyLow.INSTANCE;
            case LOWEST -> TagsUpdatedProxyLowest.INSTANCE;
        };
    }

    private static abstract class TagsUpdatedProxy extends AbstractNeoEventCommon {
        public TagsUpdatedProxy() {
            super(EventType.TAGS_UPDATED_EVENT);
        }

        @Override
        protected void registerToNeo() {
            NeoForge.EVENT_BUS.register(this);
        }

        @Override
        protected void unregisterToNeo() {
            NeoForge.EVENT_BUS.unregister(this);
        }

        @Override
        protected NeoEvent getNeoEventType(Event event) {
            return new NeoTagsUpdatedEvent(event);
        }

        protected void handle(TagsUpdatedEvent event) {
            super.onEvent(event);
        }
    }

    public static class TagsUpdatedProxyHighest extends TagsUpdatedProxy {
        static final TagsUpdatedProxyHighest INSTANCE = new TagsUpdatedProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(TagsUpdatedEvent e) { handle(e); }
    }

    public static class TagsUpdatedProxyHigh extends TagsUpdatedProxy {
        static final TagsUpdatedProxyHigh INSTANCE = new TagsUpdatedProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(TagsUpdatedEvent e) { handle(e); }
    }

    public static class TagsUpdatedProxyNormal extends TagsUpdatedProxy {
        static final TagsUpdatedProxyNormal INSTANCE = new TagsUpdatedProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(TagsUpdatedEvent e) { handle(e); }
    }

    public static class TagsUpdatedProxyLow extends TagsUpdatedProxy {
        static final TagsUpdatedProxyLow INSTANCE = new TagsUpdatedProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(TagsUpdatedEvent e) { handle(e); }
    }

    public static class TagsUpdatedProxyLowest extends TagsUpdatedProxy {
        static final TagsUpdatedProxyLowest INSTANCE = new TagsUpdatedProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(TagsUpdatedEvent e) { handle(e); }
    }
}