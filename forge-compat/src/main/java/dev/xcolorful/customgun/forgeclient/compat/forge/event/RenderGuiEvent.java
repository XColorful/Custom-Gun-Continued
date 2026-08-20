package dev.xcolorful.customgun.forgeclient.compat.forge.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public final class RenderGuiEvent extends PrepareRenderGuiEvent {

    public RenderGuiEvent(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        super(guiGraphics, partialTick);
    }
}
