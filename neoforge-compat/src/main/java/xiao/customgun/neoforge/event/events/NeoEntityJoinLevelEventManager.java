package xiao.customgun.neoforge.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEntityJoinLevelEvent;
import xiao.customgun.neoforge.event.NeoEvent;

public class NeoEntityJoinLevelEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> EntityJoinLevelProxyHighest.INSTANCE;
            case HIGH -> EntityJoinLevelProxyHigh.INSTANCE;
            case NORMAL -> EntityJoinLevelProxyNormal.INSTANCE;
            case LOW -> EntityJoinLevelProxyLow.INSTANCE;
            case LOWEST -> EntityJoinLevelProxyLowest.INSTANCE;
        };
    }

    private static abstract class EntityJoinLevelProxy extends AbstractNeoEventCommon {
        public EntityJoinLevelProxy() {
            super(EventType.ENTITY_JOIN_LEVEL_EVENT);
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
            return new NeoEntityJoinLevelEvent(event);
        }

        protected void handle(EntityJoinLevelEvent event) {
            super.onEvent(event);
        }
    }

    public static class EntityJoinLevelProxyHighest extends EntityJoinLevelProxy {
        static final EntityJoinLevelProxyHighest INSTANCE = new EntityJoinLevelProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(EntityJoinLevelEvent e) { handle(e); }
    }

    public static class EntityJoinLevelProxyHigh extends EntityJoinLevelProxy {
        static final EntityJoinLevelProxyHigh INSTANCE = new EntityJoinLevelProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(EntityJoinLevelEvent e) { handle(e); }
    }

    public static class EntityJoinLevelProxyNormal extends EntityJoinLevelProxy {
        static final EntityJoinLevelProxyNormal INSTANCE = new EntityJoinLevelProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(EntityJoinLevelEvent e) { handle(e); }
    }

    public static class EntityJoinLevelProxyLow extends EntityJoinLevelProxy {
        static final EntityJoinLevelProxyLow INSTANCE = new EntityJoinLevelProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(EntityJoinLevelEvent e) { handle(e); }
    }

    public static class EntityJoinLevelProxyLowest extends EntityJoinLevelProxy {
        static final EntityJoinLevelProxyLowest INSTANCE = new EntityJoinLevelProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(EntityJoinLevelEvent e) { handle(e); }
    }
}