package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.renderer.entity.EntityHitboxRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Deprecated(since = "1.21.6")
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    /**
     * 拦截 F3+B 实体碰撞箱渲染
     */
//    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void cgc$hideEntityHitbox(PoseStack poseStack,
                                             VertexConsumer vertexConsumer,
                                             Entity entity,
                                             float red, float green, float blue, float alpha,
                                             CallbackInfo ci) {
        if (!EntityHitboxRenderer.shouldRenderHitbox(entity)) {
            ci.cancel();
        }
    }
}
