package dev.xcolorful.customgun.client.mixin.renderer;

import dev.xcolorful.customgun.client.renderer.entity.EntityHitboxRenderer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ApiStatus.AvailableSince("1.21.11")
//@Mixin(EntityHitboxDebugRenderer.class)
public class EntityHitboxDebugRendererMixin {

    /**
     * 1.21.11 把实体碰撞箱从 render state 里挪到了独立的 debug renderer（gizmo 体系）
     * {@code EntityRenderState} 上不再有碰撞箱字段，所以拦截点也跟着搬
     * <ul>
     *     <li>{@code EntityHitboxDebugRenderer#emitGizmos} 遍历实体，逐个走 {@code showHitboxes}</li>
     *     <li>{@code showHitboxes} 重新拿得到 Entity，且盒体、乘车盒体、位置点都在里面落笔</li>
     * </ul>
     */
//    @Inject(method = "showHitboxes", at = @At("HEAD"), cancellable = true)
    private void cgc$hideEntityHitbox(Entity entity,
                                      float partialTick,
                                      boolean isServerEntity,
                                      CallbackInfo ci) {
        if (!EntityHitboxRenderer.shouldRenderHitbox(entity)) {
            ci.cancel();
        }
    }
}
