package dev.xcolorful.customgun.client.api.event;

import net.minecraft.client.gui.GuiGraphics;

public interface IPrepareRenderGuiEvent {

    GuiGraphics getGuiGraphics();

    /**
     * @return {@code RenderGuiEvent.getPartialTick().getGameTimeDeltaPartialTick(true)}
     */
    float getPartialTick();
}
