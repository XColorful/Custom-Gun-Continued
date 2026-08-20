package dev.xcolorful.customgun.forgeclient.compat.forge.event;

import dev.xcolorful.customgun.forge.CustomGunForge;
import dev.xcolorful.customgun.forgeclient.compat.forge.gui.GuiLayerRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Cancelable;
import org.jetbrains.annotations.NotNull;

@Cancelable
public class PrepareRenderOverlayEvent extends AbstractGuiEvent {

    private final LayeredDraw.Layer layer;
    private ResourceLocation registryLocation;

    public PrepareRenderOverlayEvent(GuiGraphics guiGraphics, DeltaTracker partialTick,
                                     LayeredDraw.Layer layer) {
        super(guiGraphics, partialTick);
        this.layer = layer;
    }

    public final ResourceLocation getRegistryLocation() {
        if (this.registryLocation == null) {
            this.registryLocation = GuiLayerRegistry.getRegistryLocation(this.layer);
            if (this.registryLocation == null) this.registryLocation = nullLocation;
        }

        return this.registryLocation;
    }

    private static final @NotNull ResourceLocation nullLocation = CustomGunForge.mcRegistry.createResourceLocation("minecraft:");
}
