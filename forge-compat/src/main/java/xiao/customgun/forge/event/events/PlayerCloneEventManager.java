package xiao.customgun.forge.event.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.ForgePlayerCloneEvent;

public class PlayerCloneEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PlayerCloneProxyHighest.INSTANCE;
            case HIGH -> PlayerCloneProxyHigh.INSTANCE;
            case NORMAL -> PlayerCloneProxyNormal.INSTANCE;
            case LOW -> PlayerCloneProxyLow.INSTANCE;
            case LOWEST -> PlayerCloneProxyLowest.INSTANCE;
        };
    }

    private static abstract class PlayerCloneProxy extends AbstractEventCommon {
        public PlayerCloneProxy() {
            super(EventType.PLAYER_CLONE_EVENT);
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
            return new ForgePlayerCloneEvent(event);
        }

        protected void handle(PlayerEvent.Clone event) {
            super.onEvent(event);
        }
    }

    public static class PlayerCloneProxyHighest extends PlayerCloneProxy {
        static final PlayerCloneProxyHighest INSTANCE = new PlayerCloneProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(PlayerEvent.Clone e) { handle(e); }
    }

    public static class PlayerCloneProxyHigh extends PlayerCloneProxy {
        static final PlayerCloneProxyHigh INSTANCE = new PlayerCloneProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(PlayerEvent.Clone e) { handle(e); }
    }

    public static class PlayerCloneProxyNormal extends PlayerCloneProxy {
        static final PlayerCloneProxyNormal INSTANCE = new PlayerCloneProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(PlayerEvent.Clone e) { handle(e); }
    }

    public static class PlayerCloneProxyLow extends PlayerCloneProxy {
        static final PlayerCloneProxyLow INSTANCE = new PlayerCloneProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(PlayerEvent.Clone e) { handle(e); }
    }

    public static class PlayerCloneProxyLowest extends PlayerCloneProxy {
        static final PlayerCloneProxyLowest INSTANCE = new PlayerCloneProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(PlayerEvent.Clone e) { handle(e); }
    }
}