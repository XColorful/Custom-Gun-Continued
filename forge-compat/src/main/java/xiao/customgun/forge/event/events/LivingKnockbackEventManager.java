package xiao.customgun.forge.event.events;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xiao.customgun.core.api.event.EventPriority;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEventHandler;
import xiao.customgun.forge.event.ForgeEvent;
import xiao.customgun.forge.event.ForgeLivingKnockbackEvent;

public class LivingKnockbackEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> LivingKnockbackProxyHighest.INSTANCE;
            case HIGH -> LivingKnockbackProxyHigh.INSTANCE;
            case NORMAL -> LivingKnockbackProxyNormal.INSTANCE;
            case LOW -> LivingKnockbackProxyLow.INSTANCE;
            case LOWEST -> LivingKnockbackProxyLowest.INSTANCE;
        };
    }

    private static abstract class LivingKnockbackProxy extends AbstractEventCommon {
        public LivingKnockbackProxy() {
            super(EventType.LIVING_KNOCKBACK_EVENT);
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
            return new ForgeLivingKnockbackEvent(event);
        }

        protected void handle(LivingKnockBackEvent event) {
            super.onEvent(event);
        }
    }

    public static class LivingKnockbackProxyHighest extends LivingKnockbackProxy {
        static final LivingKnockbackProxyHighest INSTANCE = new LivingKnockbackProxyHighest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(LivingKnockBackEvent e) { handle(e); }
    }

    public static class LivingKnockbackProxyHigh extends LivingKnockbackProxy {
        static final LivingKnockbackProxyHigh INSTANCE = new LivingKnockbackProxyHigh();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(LivingKnockBackEvent e) { handle(e); }
    }

    public static class LivingKnockbackProxyNormal extends LivingKnockbackProxy {
        static final LivingKnockbackProxyNormal INSTANCE = new LivingKnockbackProxyNormal();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(LivingKnockBackEvent e) { handle(e); }
    }

    public static class LivingKnockbackProxyLow extends LivingKnockbackProxy {
        static final LivingKnockbackProxyLow INSTANCE = new LivingKnockbackProxyLow();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(LivingKnockBackEvent e) { handle(e); }
    }

    public static class LivingKnockbackProxyLowest extends LivingKnockbackProxy {
        static final LivingKnockbackProxyLowest INSTANCE = new LivingKnockbackProxyLowest();
        @SubscribeEvent(priority = net.minecraftforge.eventbus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(LivingKnockBackEvent e) { handle(e); }
    }
}