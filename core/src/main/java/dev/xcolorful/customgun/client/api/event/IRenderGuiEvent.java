package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IRenderGuiEvent extends IEvent {

    /**
     * @since 26.1 {@code GuiGraphicsExtractor}
     */
    GuiGraphicsExtractor getGuiGraphics();

    /**
     * @return {@code RenderGuiEvent.getPartialTick().getGameTimeDeltaPartialTick(true)}
     */
    float getPartialTick();
}
