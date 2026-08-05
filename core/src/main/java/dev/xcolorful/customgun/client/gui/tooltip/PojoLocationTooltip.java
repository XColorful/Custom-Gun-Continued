package dev.xcolorful.customgun.client.gui.tooltip;

import dev.xcolorful.customgun.client.api.event.IItemTooltipEvent;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IEvent;
import dev.xcolorful.customgun.core.api.event.IEventHandler;
import dev.xcolorful.customgun.core.api.item.IPojoItem;
import dev.xcolorful.customgun.core.api.item.pojo.IPojoItemGetter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class PojoLocationTooltip implements IEventHandler {
    private static class PojoLocationTooltipHolder {
        private static final PojoLocationTooltip INSTANCE = new PojoLocationTooltip();
    }
    public static PojoLocationTooltip get() {
        return PojoLocationTooltipHolder.INSTANCE;
    }
    protected PojoLocationTooltip() {}
    @Override public String getEventHandlerName() {
        return this.getClass().getName();
    }
    @Override
    public void handleEvent(EventType eventType, IEvent event) {
        if (eventType == EventType.ITEM_TOOLTIP_EVENT) {
            addPojoLocationTooltip((IItemTooltipEvent) event);
        } else {
            onReceiveWrongEvent(eventType);
        }
    }

    private void addPojoLocationTooltip(IItemTooltipEvent event) {
        if (!event.getFlags().isAdvanced()) return;

        ItemStack pojoItem = event.getItemStack();
        @Nullable IPojoItem iPojoItem = IPojoItemGetter.fromItemStack(pojoItem);
        if (iPojoItem == null) return;

        if (!RenderConfig.ENABLE_RESOURCE_LOCATION_IN_TOOLTIP.get()) return;

        var pojoLocation = iPojoItem.getPojoLocation(pojoItem);
        Component pojoLocationTooltip = Component.literal(pojoLocation.toString())
//                .withStyle(ChatFormatting.DARK_GRAY); // MC用的这个
                .withStyle(ChatFormatting.GRAY); // 改这个亮一点
        event.getToolTip().add(pojoLocationTooltip); // 默认是加在底下显示
    }
}
