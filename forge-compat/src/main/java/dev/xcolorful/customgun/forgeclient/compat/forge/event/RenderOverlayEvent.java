package dev.xcolorful.customgun.forgeclient.compat.forge.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;

public final class RenderOverlayEvent extends PrepareRenderOverlayEvent {

    public RenderOverlayEvent(GuiGraphics guiGraphics, DeltaTracker partialTick,
                              LayeredDraw.Layer layer) {
        super(guiGraphics, partialTick, layer);
    }
}
