package dev.xcolorful.customgun.core.mixin.entity;

import dev.xcolorful.customgun.core.api.minecraft.entity.IEntityExtension;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ApiStatus.Internal
@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityExtension {

    @Shadow
    public boolean noPhysics;
    @Shadow
    public abstract Vec3 position();
    @Shadow
    public abstract boolean isPassenger();
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
