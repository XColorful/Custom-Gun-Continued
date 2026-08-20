package dev.xcolorful.customgun.forgeclient.compat.forge.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.eventbus.api.Event;

public abstract class AbstractGuiEvent extends Event {

    private final GuiGraphics guiGraphics;
    private final DeltaTracker partialTick;

    AbstractGuiEvent(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
    }

    public final GuiGraphics getGuiGraphics() {
        return guiGraphics;
    }
    public final DeltaTracker getPartialTick() {
        return partialTick;
    }
}
