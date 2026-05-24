package xiao.customgun.neoforge.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoEvent;
import xiao.customgun.neoforge.event.NeoPlayerCloneEvent;

public class NeoPlayerCloneEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> PlayerCloneProxyHighest.INSTANCE;
            case HIGH -> PlayerCloneProxyHigh.INSTANCE;
            case NORMAL -> PlayerCloneProxyNormal.INSTANCE;
            case LOW -> PlayerCloneProxyLow.INSTANCE;
            case LOWEST -> PlayerCloneProxyLowest.INSTANCE;
        };
    }

    private static abstract class PlayerCloneProxy extends AbstractNeoEventCommon {
        public PlayerCloneProxy() {
            super(EventType.PLAYER_CLONE_EVENT);
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
            return new NeoPlayerCloneEvent(event);
        }

        protected void handle(PlayerEvent.Clone event) {
            super.onEvent(event);
        }
    }

    public static class PlayerCloneProxyHighest extends PlayerCloneProxy {
        static final PlayerCloneProxyHighest INSTANCE = new PlayerCloneProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(PlayerEvent.Clone e) { handle(e); }
    }

    public static class PlayerCloneProxyHigh extends PlayerCloneProxy {
        static final PlayerCloneProxyHigh INSTANCE = new PlayerCloneProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(PlayerEvent.Clone e) { handle(e); }
    }

    public static class PlayerCloneProxyNormal extends PlayerCloneProxy {
        static final PlayerCloneProxyNormal INSTANCE = new PlayerCloneProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(PlayerEvent.Clone e) { handle(e); }
    }

    public static class PlayerCloneProxyLow extends PlayerCloneProxy {
        static final PlayerCloneProxyLow INSTANCE = new PlayerCloneProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(PlayerEvent.Clone e) { handle(e); }
    }

    public static class PlayerCloneProxyLowest extends PlayerCloneProxy {
        static final PlayerCloneProxyLowest INSTANCE = new PlayerCloneProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(PlayerEvent.Clone e) { handle(e); }
    }
}