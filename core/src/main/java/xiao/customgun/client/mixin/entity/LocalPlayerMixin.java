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
    private final LocalPlayer cgc$localPlayer = (LocalPlayer) (Object) this;
    private final LocalShooterProperty cgc$localShooterProperty = new LocalShooterProperty(cgc$localPlayer);

    // 行为动作
    private final LocalShooterCrawl cgc$crawl = new LocalShooterCrawl(cgc$localPlayer, cgc$localShooterProperty);
    private final LocalShooterSprint cgc$sprint = new LocalShooterSprint(cgc$localPlayer, cgc$localShooterProperty);

    // 枪械操作
    private final LocalShooterDraw cgc$draw = new LocalShooterDraw(cgc$localPlayer, cgc$localShooterProperty);
    private final LocalShooterFireSelect cgc$fireSelect = new LocalShooterFireSelect(cgc$localPlayer, cgc$localShooterProperty);
    private final LocalShooterAim cgc$aim = new LocalShooterAim(cgc$localPlayer, cgc$localShooterProperty);
    private final LocalShooterMelee cgc$melee = new LocalShooterMelee(cgc$localPlayer, cgc$localShooterProperty);
    private final LocalShooterShoot cgc$shoot = new LocalShooterShoot(cgc$localPlayer, cgc$localShooterProperty);
    private final LocalShooterBolt cgc$bolt = new LocalShooterBolt(cgc$localPlayer, cgc$localShooterProperty);
    private final LocalShooterReload cgc$reload = new LocalShooterReload(cgc$localPlayer, cgc$localShooterProperty);

    private final LocalShooterInspect cgc$inspect = new LocalShooterInspect(cgc$localPlayer, cgc$localShooterProperty);

    @Inject(method = "tick", at = @At("HEAD"))
    public void onLocalTick(CallbackInfo ci) {
        if (cgc$localPlayer.level().isClientSide()) {
            this.cgc$aim.tickAimingProgress();
            this.cgc$crawl.tickCrawl();
            this.cgc$localShooterProperty.tickStateLock();
            this.cgc$bolt.tickAutoBolt();
            cgc$localPlayer.setSprinting(this.cgc$sprint.getProcessedSprintStatus(cgc$localPlayer.isSprinting()));
        }
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setSprinting(Z)V"))
    public void swapSprintStatus(LocalPlayer player, boolean sprinting, Operation<Void> original) {
        if (sprinting) {
            this.cgc$reload.cancelReload();
        }
        original.call(player, this.cgc$sprint.getProcessedSprintStatus(sprinting));
    }

    @Inject(method = "respawn", at = @At("RETURN"))
    public void onRespawn(CallbackInfo ci) {
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
    public void cgc$crawl(boolean isCrawl) {
        this.cgc$crawl.crawl(isCrawl);
    }

    // --------枪械操作--------

    @Override public void cgc$clientDraw(ItemStack lastItem) {
        this.cgc$draw.draw(lastItem);
    }
    @Override public void cgc$resetDraw() {
        this.cgc$draw.setReadyToDraw(false);
    }

    @Override
    public void cgc$fireSelect() {
        this.cgc$fireSelect.fireSelect();
    }

    @Override
    public void cgc$aim(boolean isAim) {
        this.cgc$aim.aim(isAim);
    }

    @Override
    public void cgc$melee() {
        this.cgc$melee.melee();
    }

    @Override public ShootResult cgc$localShoot() {
        this.cgc$reload.cancelReload();
        return this.cgc$shoot.shoot();
    }
    @Override public boolean cgc$chargeShoot(boolean isCharge) {
        return this.cgc$shoot.chargeShoot(isCharge);
    }

    @Override
    public void cgc$bolt() {
        this.cgc$bolt.bolt();
    }

    @Override
    public void cgc$reload() {
        this.cgc$reload.reload();
    }

    @Override public void cgc$inspect() {
        this.cgc$inspect.inspect();
    }

    // --------ILocalShooterState--------

    @Override public boolean cgc$isAim() {
        return this.cgc$aim.isAim();
    }
    @Override public boolean cgc$isCrawl() {
        return this.cgc$crawl.isCrawling();
    }
    @Override public boolean cgc$isReadyToDraw() {
        return this.cgc$draw.isReadyToDraw();
    }
    @Override public boolean cgc$isCharging() {
        return this.cgc$localShooterProperty.isCharging;
    }
    @Override public float cgc$getAimingProgress(float partialTicks) {
        return this.cgc$aim.getClientAimingProgress(partialTicks);
    }
    @Override public long cgc$getShootCooldown() {
        return this.cgc$shoot.getClientShootCooldown();
    }
    @Override public float cgc$getChargeProgress() {
        return this.cgc$localShooterProperty.chargeProgress;
    }
}
