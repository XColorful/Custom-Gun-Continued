package xiao.customgun.forge.event.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeEntityJoinLevelEvent;
import xiao.customgun.forge.event.ForgeEvent;

public class EntityJoinLevelEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> EntityJoinLevelProxyHighest.INSTANCE;
            case HIGH -> EntityJoinLevelProxyHigh.INSTANCE;
            case NORMAL -> EntityJoinLevelProxyNormal.INSTANCE;
            case LOW -> EntityJoinLevelProxyLow.INSTANCE;
            case LOWEST -> EntityJoinLevelProxyLowest.INSTANCE;
        };
    }

    private static abstract class EntityJoinLevelProxy extends AbstractEventCommon {
        public EntityJoinLevelProxy() {
            super(EventType.ENTITY_JOIN_LEVEL_EVENT);
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
            return new ForgeEntityJoinLevelEvent(event);
        }

        protected void handle(EntityJoinLevelEvent event) {
            super.onEvent(event);
        }
    }

    public static class EntityJoinLevelProxyHighest extends EntityJoinLevelProxy {
        static final EntityJoinLevelProxyHighest INSTANCE = new EntityJoinLevelProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(EntityJoinLevelEvent e) { handle(e); }
    }

    public static class EntityJoinLevelProxyHigh extends EntityJoinLevelProxy {
        static final EntityJoinLevelProxyHigh INSTANCE = new EntityJoinLevelProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(EntityJoinLevelEvent e) { handle(e); }
    }

    public static class EntityJoinLevelProxyNormal extends EntityJoinLevelProxy {
        static final EntityJoinLevelProxyNormal INSTANCE = new EntityJoinLevelProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(EntityJoinLevelEvent e) { handle(e); }
    }

    public static class EntityJoinLevelProxyLow extends EntityJoinLevelProxy {
        static final EntityJoinLevelProxyLow INSTANCE = new EntityJoinLevelProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(EntityJoinLevelEvent e) { handle(e); }
    }

    public static class EntityJoinLevelProxyLowest extends EntityJoinLevelProxy {
        static final EntityJoinLevelProxyLowest INSTANCE = new EntityJoinLevelProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(EntityJoinLevelEvent e) { handle(e); }
    }
}