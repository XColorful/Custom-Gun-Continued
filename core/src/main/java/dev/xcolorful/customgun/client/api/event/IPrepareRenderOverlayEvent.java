package dev.xcolorful.customgun.client.api.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public interface IPrepareRenderOverlayEvent {

    GuiGraphicsExtractor getGuiGraphics();

    /**
     * @return {@code RenderGuiOverlayEvent.getPartialTick().getGameTimeDeltaPartialTick(true)}
     */
    float getPartialTick();

    Identifier getRegistryLocation();
}
