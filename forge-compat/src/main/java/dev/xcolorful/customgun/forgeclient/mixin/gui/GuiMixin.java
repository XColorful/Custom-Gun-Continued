package dev.xcolorful.customgun.forgeclient.mixin.gui;

import dev.xcolorful.customgun.forgeclient.compat.forge.event.PrepareRenderGuiEvent;
import dev.xcolorful.customgun.forgeclient.compat.forge.event.RenderGuiEvent;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cgc$onBeforeRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker,
                                CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new PrepareRenderGuiEvent(guiGraphics, deltaTracker))) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void cgc$onAfterRender(GuiGraphics guiGraphics, DeltaTracker deltaTracker,
                               CallbackInfo ci) {
        if (ci.isCancelled()) return; // 被Mixin提前返回

        MinecraftForge.EVENT_BUS.post(new RenderGuiEvent(guiGraphics, deltaTracker));
    }
}
