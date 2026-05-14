package xiao.customgun.forge.event.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeDatapackSyncEvent;
import xiao.customgun.forge.event.ForgeEvent;

public class DatapackSyncEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> DatapackSyncProxyHighest.INSTANCE;
            case HIGH -> DatapackSyncProxyHigh.INSTANCE;
            case NORMAL -> DatapackSyncProxyNormal.INSTANCE;
            case LOW -> DatapackSyncProxyLow.INSTANCE;
            case LOWEST -> DatapackSyncProxyLowest.INSTANCE;
        };
    }

    private static abstract class DatapackSyncProxy extends AbstractEventCommon {
        public DatapackSyncProxy() {
            super(EventType.DATAPACK_SYNC_EVENT);
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
            return new ForgeDatapackSyncEvent(event);
        }

        protected void handle(OnDatapackSyncEvent event) {
            super.onEvent(event);
        }
    }

    public static class DatapackSyncProxyHighest extends DatapackSyncProxy {
        static final DatapackSyncProxyHighest INSTANCE = new DatapackSyncProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(OnDatapackSyncEvent e) { handle(e); }
    }

    public static class DatapackSyncProxyHigh extends DatapackSyncProxy {
        static final DatapackSyncProxyHigh INSTANCE = new DatapackSyncProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(OnDatapackSyncEvent e) { handle(e); }
    }

    public static class DatapackSyncProxyNormal extends DatapackSyncProxy {
        static final DatapackSyncProxyNormal INSTANCE = new DatapackSyncProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(OnDatapackSyncEvent e) { handle(e); }
    }

    public static class DatapackSyncProxyLow extends DatapackSyncProxy {
        static final DatapackSyncProxyLow INSTANCE = new DatapackSyncProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(OnDatapackSyncEvent e) { handle(e); }
    }

    public static class DatapackSyncProxyLowest extends DatapackSyncProxy {
        static final DatapackSyncProxyLowest INSTANCE = new DatapackSyncProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(OnDatapackSyncEvent e) { handle(e); }
    }
}