package dev.xcolorful.customgun.forgeclient.mixin.renderer;

import dev.xcolorful.customgun.client.api.renderer.item.ItemDisplayContextTracker;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/*
为了跨版本提前知道26.1neoforge的移植方式，添加此类作为占位符
 */
//@Mixin(ItemStackRenderState.class)
public class ItemStackRenderStateMixin {

    ItemDisplayContext displayContext;

    private void cgc$captureDisplayContext(CallbackInfo ci) {
        ItemDisplayContextTracker.push(this.displayContext);
    }

    private void cgc$clearDisplayContext(CallbackInfo ci) {
        ItemDisplayContextTracker.pop();
    }
}
