package dev.xcolorful.customgun.client.api.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IPrepareRenderGuiEvent {

    GuiGraphicsExtractor getGuiGraphics();

    /**
     * @return {@code RenderGuiEvent.getPartialTick().getGameTimeDeltaPartialTick(true)}
     */
    float getPartialTick();
}
