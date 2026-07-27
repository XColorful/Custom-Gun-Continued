/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.client.api.entity.ILocalShooter;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.entity.shooter.*;
import xiao.customgun.core.api.entity.ShootResult;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin implements ILocalShooter {
    private final LocalPlayer cgc$localShooter = (LocalPlayer) (Object) this;
    private final LocalShooterProperty cgc$localShooterProperty = new LocalShooterProperty(cgc$localShooter);

    // 行为动作
    private final LocalShooterProne cgc$localProne = new LocalShooterProne(cgc$localShooter, cgc$localShooterProperty);
    private final LocalShooterSprint cgc$localSprint = new LocalShooterSprint(cgc$localShooter, cgc$localShooterProperty);

    // 枪械操作
    private final LocalShooterDraw cgc$localDraw = new LocalShooterDraw(cgc$localShooter, cgc$localShooterProperty);
    private final LocalShooterSwitchFireMode cgc$localSwitchFireMode = new LocalShooterSwitchFireMode(cgc$localShooter, cgc$localShooterProperty);
    private final LocalShooterAim cgc$localAim = new LocalShooterAim(cgc$localShooter, cgc$localShooterProperty);
    private final LocalShooterMelee cgc$localMelee = new LocalShooterMelee(cgc$localShooter, cgc$localShooterProperty);
    private final LocalShooterShoot cgc$localShoot = new LocalShooterShoot(cgc$localShooter, cgc$localShooterProperty);
    private final LocalShooterBolt cgc$localBolt = new LocalShooterBolt(cgc$localShooter, cgc$localShooterProperty);
    private final LocalShooterReload cgc$localReload = new LocalShooterReload(cgc$localShooter, cgc$localShooterProperty);

    private final LocalShooterInspect cgc$localInspect = new LocalShooterInspect(cgc$localShooter, cgc$localShooterProperty);

    @Inject(method = "tick", at = @At("HEAD"))
    public void cgc$onLocalTick(CallbackInfo ci) {
        if (cgc$localShooter.level().isClientSide()) {
            this.cgc$localAim.tickAimingProgress();
            this.cgc$localProne.tickProne();
            this.cgc$localShooterProperty.tickStateLock();
            this.cgc$localBolt.tickAutoBolt();
            cgc$localShooter.setSprinting(this.cgc$localSprint.getProcessedSprintStatus(cgc$localShooter.isSprinting()));
        }
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V"))
    public void cgc$swapSprintStatus(LocalPlayer player, boolean sprinting, Operation<Void> original) {
        if (sprinting) {
            this.cgc$localReload.cancelReload();
        }
        original.call(player, this.cgc$localSprint.getProcessedSprintStatus(sprinting));
    }

    @Inject(method = "respawn", at = @At("RETURN"))
    public void cgc$onRespawn(CallbackInfo ci) {
        this.cgc$localShooterProperty.resetProperty();
        this.cgc$clientDraw(ItemStack.EMPTY);
    }

    // --------ILocalShooter--------

    @Override public LocalShooterProperty cgc$getLocalShooterProperty() {
        return this.cgc$localShooterProperty;
    }

    // --------IClientGunOperator--------

    // --------行为动作--------

    @Override
    public void cgc$prone(boolean isProne) {
        this.cgc$localProne.prone(isProne);
    }

    // --------枪械操作--------

    @Override public void cgc$clientDraw(ItemStack lastItem) {
        this.cgc$localDraw.draw(lastItem);
    }
    @Override public void cgc$resetDraw() {
        this.cgc$localDraw.setReadyToDraw(false);
    }

    @Override
    public void cgc$switchFireMode() {
        this.cgc$localSwitchFireMode.switchFireMode();
    }

    @Override
    public void cgc$aim(boolean isAim) {
        this.cgc$localAim.aim(isAim);
    }

    @Override
    public void cgc$melee() {
        this.cgc$localMelee.melee();
    }

    @Override public ShootResult cgc$localShoot() {
        this.cgc$localReload.cancelReload();
        return this.cgc$localShoot.shoot();
    }
    @Override public boolean cgc$chargeAndGetResult(boolean doShoot) {
        return this.cgc$localShoot.chargeAndGetResult(doShoot);
    }

    @Override
    public void cgc$bolt() {
        this.cgc$localBolt.bolt();
    }

    @Override
    public void cgc$reload() {
        this.cgc$localReload.reload();
    }

    @Override public void cgc$inspect() {
        this.cgc$localInspect.inspect();
    }

    // --------ILocalShooterState--------

    @Override public boolean cgc$isAim() {
        return this.cgc$localAim.isAim();
    }
    @Override public boolean cgc$isProne() {
        return this.cgc$localProne.isProne();
    }
    @Override public boolean cgc$isReadyToDraw() {
        return this.cgc$localDraw.isReadyToDraw();
    }
    @Override public boolean cgc$isCharging() {
        return this.cgc$localShooterProperty.isCharging;
    }
    @Override public float cgc$getAimingProgress(float partialTicks) {
        return this.cgc$localAim.getClientAimingProgress(partialTicks);
    }
    @Override public long cgc$getShootCooldown() {
        return this.cgc$localShoot.getClientShootCooldown();
    }
    @Override public float cgc$getChargeProgress() {
        return this.cgc$localShooterProperty.chargeProgress;
    }
}
