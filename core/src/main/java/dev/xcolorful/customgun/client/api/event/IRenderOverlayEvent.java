package dev.xcolorful.customgun.client.api.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;

public interface IRenderOverlayEvent {

    GuiGraphics getGuiGraphics();

    /**
     * @return {@code RenderGuiOverlayEvent.getPartialTick().getGameTimeDeltaPartialTick(true)}
     */
    float getPartialTick();

    Identifier getRegistryLocation();
}
