package dev.xcolorful.customgun.forgeclient.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.xcolorful.customgun.forgeclient.compat.forge.event.PrepareRenderOverlayEvent;
import dev.xcolorful.customgun.forgeclient.compat.forge.event.RenderOverlayEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LayeredDraw.class)
public class LayeredDrawMixin {

    @WrapOperation(
            method = "renderInner",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/LayeredDraw$Layer;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"
            )
    )
    private void cgc$wrapLayerRender(LayeredDraw.Layer layer, GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (MinecraftForge.EVENT_BUS.post(new PrepareRenderOverlayEvent(guiGraphics, deltaTracker, layer))) {
            return;
        }

        original.call(layer, guiGraphics, deltaTracker);

        MinecraftForge.EVENT_BUS.post(new RenderOverlayEvent(guiGraphics, deltaTracker, layer));
    }
}