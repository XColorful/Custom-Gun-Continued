package dev.xcolorful.customgun.client.mixin.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.xcolorful.customgun.client.model.bake.StaticBatch;
import dev.xcolorful.customgun.client.renderer.entity.EntityHitboxRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// @Deprecated(since = "1.21.6")
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    /**
     * 拦截 F3+B 实体碰撞箱渲染
     */
    @Inject(method = "renderHitbox", at = @At("HEAD"), cancellable = true)
    private static void cgc$hideEntityHitbox(PoseStack poseStack,
                                             VertexConsumer vertexConsumer,
                                             Entity entity,
                                             float partialTicks,
                                             CallbackInfo ci) {
        if (!EntityHitboxRenderer.shouldRenderHitbox(entity)) {
            ci.cancel();
        }
    }

    /**
     * 静态合批需要实例身份（渲染重构B2）：{@code renderByItem} 只能拿到姿态，拿不到实体
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void cgc$pushEntity(Entity entity,
                                double x, double y, double z,
                                float yaw, float partialTicks,
                                PoseStack poseStack,
                                MultiBufferSource bufferSource,
                                int light,
                                CallbackInfo ci) {
        StaticBatch.pushEntity(entity);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void cgc$popEntity(Entity entity,
                               double x, double y, double z,
                               float yaw, float partialTicks,
                               PoseStack poseStack,
                               MultiBufferSource bufferSource,
                               int light,
                               CallbackInfo ci) {
        StaticBatch.popEntity();
    }
}
