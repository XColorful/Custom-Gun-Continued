package xiao.customgun.neoforge.event.events;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.neoforge.event.NeoDatapackSyncEvent;
import xiao.customgun.neoforge.event.NeoEvent;

public class NeoDatapackSyncEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> DatapackSyncProxyHighest.INSTANCE;
            case HIGH -> DatapackSyncProxyHigh.INSTANCE;
            case NORMAL -> DatapackSyncProxyNormal.INSTANCE;
            case LOW -> DatapackSyncProxyLow.INSTANCE;
            case LOWEST -> DatapackSyncProxyLowest.INSTANCE;
        };
    }

    private static abstract class DatapackSyncProxy extends AbstractNeoEventCommon {
        public DatapackSyncProxy() {
            super(EventType.DATAPACK_SYNC_EVENT);
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
            return new NeoDatapackSyncEvent(event);
        }

        protected void handle(OnDatapackSyncEvent event) {
            super.onEvent(event);
        }
    }

    public static class DatapackSyncProxyHighest extends DatapackSyncProxy {
        static final DatapackSyncProxyHighest INSTANCE = new DatapackSyncProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(OnDatapackSyncEvent e) { handle(e); }
    }

    public static class DatapackSyncProxyHigh extends DatapackSyncProxy {
        static final DatapackSyncProxyHigh INSTANCE = new DatapackSyncProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(OnDatapackSyncEvent e) { handle(e); }
    }

    public static class DatapackSyncProxyNormal extends DatapackSyncProxy {
        static final DatapackSyncProxyNormal INSTANCE = new DatapackSyncProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(OnDatapackSyncEvent e) { handle(e); }
    }

    public static class DatapackSyncProxyLow extends DatapackSyncProxy {
        static final DatapackSyncProxyLow INSTANCE = new DatapackSyncProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(OnDatapackSyncEvent e) { handle(e); }
    }

    public static class DatapackSyncProxyLowest extends DatapackSyncProxy {
        static final DatapackSyncProxyLowest INSTANCE = new DatapackSyncProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(OnDatapackSyncEvent e) { handle(e); }
    }
}