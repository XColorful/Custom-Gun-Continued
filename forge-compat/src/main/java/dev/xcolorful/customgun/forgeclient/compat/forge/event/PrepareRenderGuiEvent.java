package dev.xcolorful.customgun.forgeclient.compat.forge.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class PrepareRenderGuiEvent extends AbstractGuiEvent {

    public PrepareRenderGuiEvent(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        super(guiGraphics, partialTick);
    }
}
