package dev.xcolorful.customgun.client.api.event;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public interface IPrepareRenderOverlayEvent {

    GuiGraphics getGuiGraphics();

    /**
     * @return {@code RenderGuiOverlayEvent.getPartialTick().getGameTimeDeltaPartialTick(true)}
     */
    float getPartialTick();

    ResourceLocation getRegistryLocation();
}
