package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public interface IPrepareRenderOverlayEvent extends IEvent {

    GuiGraphics getGuiGraphics();

    /**
     * @return {@code RenderGuiOverlayEvent.getPartialTick().getGameTimeDeltaPartialTick(true)}
     */
    float getPartialTick();

    ResourceLocation getRegistryLocation();
}
