package dev.xcolorful.customgun.client.mixin.renderer;

import dev.xcolorful.customgun.client.api.renderer.item.ItemDisplayContextTracker;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
为了跨版本提前知道26.1neoforge的移植方式，添加此类作为占位符
 */
@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin {

    @Shadow
    ItemDisplayContext displayContext;

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V", at = @At("HEAD"))
    private void cgc$captureDisplayContext(CallbackInfo ci) {
        ItemDisplayContextTracker.push(this.displayContext);
    }

    @Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V", at = @At("TAIL"))
    private void cgc$clearDisplayContext(CallbackInfo ci) {
        ItemDisplayContextTracker.pop();
    }
}
