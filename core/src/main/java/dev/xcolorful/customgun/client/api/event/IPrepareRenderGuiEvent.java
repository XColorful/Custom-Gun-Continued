package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.gui.GuiGraphics;

public interface IPrepareRenderGuiEvent extends IEvent {

    GuiGraphics getGuiGraphics();

    /**
     * @return {@code RenderGuiEvent.getPartialTick().getGameTimeDeltaPartialTick(true)}
     */
    float getPartialTick();
}
