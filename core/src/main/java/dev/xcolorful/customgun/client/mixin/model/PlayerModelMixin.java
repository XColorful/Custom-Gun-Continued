package dev.xcolorful.customgun.client.mixin.model;

import dev.xcolorful.customgun.client.api.renderer.KeepingItemRenderer;
import dev.xcolorful.customgun.client.util.ClientRenderUtils;
import dev.xcolorful.customgun.core.api.item.animation.IAnimationItemGetter;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {
    @Shadow
    @Final public ModelPart leftSleeve;

    @Shadow
    @Final public ModelPart rightSleeve;

    public PlayerModelMixin(ModelPart part) {
        super(part);
    }

    /**
     * 用于清除默认的手臂旋转
     * <p>
     * TaCZ 里这条注入是活的，移植到本仓库后不再命中（实测加日志不触发），两条原因：
     * <ul>
     *     主因
     *     <li>TaCZ 的第一人称手臂走 {@code RenderHelper#renderFirstPersonArm} → {@code PlayerRenderer#renderRightHand}，于是 {@code PlayerRenderer#renderHand} 以全零参数调用 {@code setupAnim} 正好打到这里</li>
     *     <li>CGC 改由 {@link dev.xcolorful.customgun.client.renderer.model.HandRender} 在 {@code righthand_pos} / {@code lefthand_pos} 节点上直接调用
     *     {@link dev.xcolorful.customgun.client.util.ClientRenderHelper#renderFirstPersonArm}，不再碰 {@code renderRightHand}，这条路径整个消失</li>
     *     次因
     *     <li>剩下的原版路径（主手为空时 {@code ItemInHandRenderer#renderArmWithItem} 走到 {@code renderPlayerArm}）要求 {@code mainHandItem} 为空，而这恰好就是 {@link KeepingItemRenderer#cgc$getCurrentItem()} 取不到动画物品的时候；keep 期间两者虽然 都指向被 keep 的物品，但该物品非空，走的是「持有物品」分支，手臂根本不参与渲染。</li>
     *     <li>TaCZ 的 {@code cancelEquippedProgress} 整段是注释掉的，{@code mainHandHeight} 衰减三个 tick 后 {@code mainHandItem} 会回落到实际（可能为空）的手，所以 TaCZ 那条路反而通</li>
     * </ul>
     * 手臂归零这件事已由 CGC 在别处重复了一遍：
     * {@link dev.xcolorful.customgun.client.util.ClientRenderHelper#renderFirstPersonArm} 直接对
     * {@code arm} 调 {@code resetPose()} 并写回 {@code zRot = ±0.1F}（等于
     * {@code AnimationUtils#bobModelPart} 在 {@code ageInTicks == 0} 时的贡献），
     * 本注入目前是未使用的
     */
    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At(value = "TAIL"))
    private void cgc$setRotationAnglesTail(AvatarRenderState renderState,
                                       CallbackInfo ci) {
        @Nullable LivingEntity entityIn = ClientRenderUtils.RenderState.getLivingEntity(renderState);
        float ageInTicks = renderState.ageInTicks;

        if (!(entityIn instanceof Player player)) {
            return;
        }

        // 用于清除默认的手臂旋转
        ItemStack currentItem = KeepingItemRenderer.cgc$getRenderer().cgc$getCurrentItem();
        if (ageInTicks == 0F // 第一人称渲染时
                && IAnimationItemGetter.fromItemStack(currentItem) != null) {
            // ↓这里实际上已经不会触发了
            cgc$resetRotation(this.rightArm);
            cgc$resetRotation(this.leftArm);
            {
                // [1.20.1, 1.21.4)
//                this.rightSleeve.copyFrom(this.rightArm);
//                this.leftSleeve.copyFrom(this.leftArm);

                // [1.21.4, )
            }
        }
    }

    /**
     * 将给定模型的旋转角度和旋转点重置为零
     */
    private void cgc$resetRotation(ModelPart part) {
        part.xRot = 0.0F;
        part.yRot = 0.0F;
        part.zRot = 0.0F;
    }
}
