package dev.xcolorful.customgun.client.mixin.renderer;

import dev.xcolorful.customgun.client.renderer.entity.EntityHitboxRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ApiStatus.AvailableSince("1.21.6")
@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    /**
     * <ul>
     *     1.21.2 起实体渲染拆成「抽取 render state → 渲染 state」两步
     *     <li>抽取（{@link EntityRenderer#createRenderState}）时还拿着 Entity</li>
     *     <li>抽完之后碰撞箱只剩{@code HitboxRenderState}（纯几何 + 颜色，没有 Entity）</li>
     *     <li>所以拦截点必须放在抽取之后</li>
     * </ul>
     */
    @Inject(
            method = "createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("RETURN")
    )
    private void cgc$hideEntityHitbox(Entity entity,
                                      float partialTick,
                                      CallbackInfoReturnable<EntityRenderState> cir) {
        if (!EntityHitboxRenderer.shouldRenderHitbox(entity)) {
            // 清空抽取结果，渲染阶段（EntityRenderDispatcher 里 hitboxesRenderState != null 的分支）自然跳过
            EntityRenderState renderState = cir.getReturnValue();
            renderState.hitboxesRenderState = null;
            renderState.serverHitboxesRenderState = null;
        }
    }
}
