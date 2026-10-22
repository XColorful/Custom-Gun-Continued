package dev.xcolorful.customgun.core.mixin.entity;

import dev.xcolorful.customgun.core.api.minecraft.entity.IEntityExtension;
import dev.xcolorful.customgun.core.api.resource.data.tag.BypassBulletType;
import dev.xcolorful.customgun.core.init.registry.ModDamageTypes;
import dev.xcolorful.customgun.core.util.EntityUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@ApiStatus.Internal
@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityExtension {

    @Unique
    private final Entity cgc$entity = (Entity) (Object) this;

    @Shadow
    public boolean noPhysics;
    @Shadow
    public abstract Vec3 position();
    @Shadow
    public abstract boolean isPassenger();
    @Shadow
    public abstract EntityType<?> getType();
    @Shadow
    protected abstract Entity.MovementEmission getMovementEmission();

    /**
     * 1.20.1 vanilla 的{@code Entity.walkDist}在1.21.4已被移除，手动维护一份跨版本统一的水平移动距离
     * 累加位置、守卫与系数都与 vanilla 一致
     */
    @Unique
    private float cgc$horizontalMoveDist = 0f;
    @Unique
    private float cgc$horizontalMoveDistOld = 0f;
    @Unique
    private Vec3 cgc$moveStartPosition = Vec3.ZERO;

    // --------Entity--------

    // 对应 vanilla 在 baseTick 开头的 walkDistO = walkDist
    @Inject(method = "baseTick", at = @At(value = "HEAD"))
    private void cgc$beforeBaseTick(CallbackInfo ci) {
        this.cgc$horizontalMoveDistOld = this.cgc$horizontalMoveDist;
    }

    // 对应 vanilla 在 move 收尾处的 walkDist += ...
    @Inject(method = "move", at = @At(value = "HEAD"))
    private void cgc$beforeMove(MoverType moverType, Vec3 movement, CallbackInfo ci) {
        this.cgc$moveStartPosition = this.position();
    }
    @Inject(method = "move", at = @At(value = "RETURN"))
    private void cgc$afterMove(MoverType moverType, Vec3 movement, CallbackInfo ci) {
        if (this.noPhysics || this.isPassenger() || !this.getMovementEmission().emitsAnything()) return;

        Vec3 actualMovement = this.position().subtract(this.cgc$moveStartPosition);
        this.cgc$horizontalMoveDist += (float) actualMovement.horizontalDistance() * 0.6F;
    }

    /*
    1.21.4 起 Entity.isInvulnerableTo 下沉为 isInvulnerableToBase
    由 LivingEntity.isInvulnerableTo(ServerLevel, DamageSource) 调用
     */
    @Inject(method = "isInvulnerableToBase", at = @At("RETURN"), cancellable = true)
    private void cgc$projectileInvulnerability(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || !source.is(ModDamageTypes.BULLET_DAMAGE)) return;

        if (!source.is(ModDamageTypes.PIERCE_DAMAGE)
                && EntityUtils.is(cgc$entity, BypassBulletType.ENTITY.getArmorEnchantment())) {
            cir.setReturnValue(true);
        } else if (!source.is(ModDamageTypes.BYPASS_DAMAGE)
                && EntityUtils.is(cgc$entity, BypassBulletType.ENTITY.getInvulnerabilityEnchantment())) {
            cir.setReturnValue(true);
        }
    }

    // --------IEntityExtension--------

    @Unique
    @Override
    public float cgc$getHorizontalMoveDist() {
        return this.cgc$horizontalMoveDist;
    }
    @Unique
    @Override
    public float cgc$getHorizontalMoveDistOld() {
        return this.cgc$horizontalMoveDistOld;
    }
}
