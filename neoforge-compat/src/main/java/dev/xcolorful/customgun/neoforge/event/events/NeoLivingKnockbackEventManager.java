package dev.xcolorful.customgun.neoforge.event.events;

import dev.xcolorful.customgun.core.api.event.EventPriority;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import dev.xcolorful.customgun.neoforge.event.NeoLivingKnockbackEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

public class NeoLivingKnockbackEventManager {

    public static boolean register(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).addEventHandler(eventHandler, receiveCanceled);
    }

    public static boolean unregister(IEventHandler eventHandler, EventPriority priority, boolean receiveCanceled) {
        return getProxy(priority).removeEventHandler(eventHandler, receiveCanceled);
    }

    private static AbstractNeoEventCommon getProxy(EventPriority priority) {
        return switch (priority) {
            case HIGHEST -> LivingKnockbackProxyHighest.INSTANCE;
            case HIGH -> LivingKnockbackProxyHigh.INSTANCE;
            case NORMAL -> LivingKnockbackProxyNormal.INSTANCE;
            case LOW -> LivingKnockbackProxyLow.INSTANCE;
            case LOWEST -> LivingKnockbackProxyLowest.INSTANCE;
        };
    }

    private static abstract class LivingKnockbackProxy extends AbstractNeoEventCommon {
        public LivingKnockbackProxy() {
            super(EventType.LIVING_KNOCKBACK_EVENT);
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
            return new NeoLivingKnockbackEvent(event);
        }

        protected void handle(LivingKnockBackEvent event) {
            super.onEvent(event);
        }
    }

    public static class LivingKnockbackProxyHighest extends LivingKnockbackProxy {
        static final LivingKnockbackProxyHighest INSTANCE = new LivingKnockbackProxyHighest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST, receiveCanceled = true)
        public void onEvent(LivingKnockBackEvent e) { handle(e); }
    }

    public static class LivingKnockbackProxyHigh extends LivingKnockbackProxy {
        static final LivingKnockbackProxyHigh INSTANCE = new LivingKnockbackProxyHigh();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGH, receiveCanceled = true)
        public void onEvent(LivingKnockBackEvent e) { handle(e); }
    }

    public static class LivingKnockbackProxyNormal extends LivingKnockbackProxy {
        static final LivingKnockbackProxyNormal INSTANCE = new LivingKnockbackProxyNormal();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.NORMAL, receiveCanceled = true)
        public void onEvent(LivingKnockBackEvent e) { handle(e); }
    }

    public static class LivingKnockbackProxyLow extends LivingKnockbackProxy {
        static final LivingKnockbackProxyLow INSTANCE = new LivingKnockbackProxyLow();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOW, receiveCanceled = true)
        public void onEvent(LivingKnockBackEvent e) { handle(e); }
    }

    public static class LivingKnockbackProxyLowest extends LivingKnockbackProxy {
        static final LivingKnockbackProxyLowest INSTANCE = new LivingKnockbackProxyLowest();
        @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.LOWEST, receiveCanceled = true)
        public void onEvent(LivingKnockBackEvent e) { handle(e); }
    }
}