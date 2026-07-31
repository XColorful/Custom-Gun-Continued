/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.mixin.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xiao.customgun.core.api.entity.*;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.api.projectile.physics.IProjectilePhysicsRuntime;
import xiao.customgun.core.entity.LivingShooterSyncKey;
import xiao.customgun.core.entity.shooter.modifier.ShooterGunModifierManager;
import xiao.customgun.core.entity.shooter.*;
import xiao.customgun.core.projectile.impact.ProjectileImpactManager;

import java.util.function.Supplier;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements ILivingShooter, IBulletVictimEntity {
    private final LivingEntity cgc$shooter = (LivingEntity) (Object) this;
    private final ShooterProperty cgc$shooterProperty = new ShooterProperty();

    // 行为动作
    private final LivingShooterProne cgc$prone = new LivingShooterProne(cgc$shooter, cgc$shooterProperty);
    private final LivingShooterSprint cgc$sprint = new LivingShooterSprint(cgc$shooter, cgc$shooterProperty);

    // 枪械操作
    private final LivingShooterDraw cgc$draw = new LivingShooterDraw(cgc$shooter, cgc$shooterProperty);
    private final LivingShooterSwitchFireMode cgc$switchFireMode = new LivingShooterSwitchFireMode(cgc$shooter, cgc$shooterProperty);
    private final LivingShooterAim cgc$aim = new LivingShooterAim(cgc$shooter, cgc$shooterProperty);
    private final LivingShooterMelee cgc$melee = new LivingShooterMelee(cgc$shooter, cgc$shooterProperty, cgc$draw);
    private final LivingShooterShoot cgc$shoot = new LivingShooterShoot(cgc$shooter, cgc$shooterProperty, cgc$draw);
    private final LivingShooterBolt cgc$bolt = new LivingShooterBolt(cgc$shooter, cgc$shooterProperty, cgc$draw, cgc$shoot);
    private final LivingShooterReload cgc$reload = new LivingShooterReload(cgc$shooter, cgc$shooterProperty, cgc$draw, cgc$shoot);

    // 生物本体影响
    private final LivingShooterSpeedModifier cgc$speed = new LivingShooterSpeedModifier(cgc$shooter, cgc$shooterProperty);

    // 持枪者属性
    private final LivingShooterAmmoCheck cgc$ammoCheck = new LivingShooterAmmoCheck(cgc$shooter, cgc$shooterProperty);

    // 枪械附加属性
    private final LivingShooterHeat cgc$heat = new LivingShooterHeat(cgc$shooter, cgc$shooterProperty);


    private float cgc$knockbackStrength;

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void cgc$onLivingTick(CallbackInfo ci) {
        if (level().isClientSide()) {
            return;
        }
        // 完成各种 tick 任务
        ReloadState reloadState = this.cgc$reload.tickReloadState();
        this.cgc$aim.tickAimingProgress();
        this.cgc$aim.tickSprint();
        this.cgc$prone.tickProne();
        this.cgc$bolt.tickBolt();
        this.cgc$melee.scheduleTickMelee();
        this.cgc$speed.updateSpeedModifier();
        this.cgc$heat.tickHeat();
        cgc$shooter.setSprinting(this.cgc$getProcessedSprintStatus(this.cgc$shooter.isSprinting()));
        // 从服务端同步数据
        LivingShooterSyncKey.SHOOT_COOL_DOWN_KEY.setValue(cgc$shooter, this.cgc$shoot.getShootCooldown());
        LivingShooterSyncKey.MELEE_COOL_DOWN_KEY.setValue(cgc$shooter, this.cgc$melee.getMeleeCooldownMs(System.currentTimeMillis()));
        LivingShooterSyncKey.DRAW_COOL_DOWN_KEY.setValue(cgc$shooter, this.cgc$draw.getDrawCooldown());
        LivingShooterSyncKey.IS_BOLTING_KEY.setValue(cgc$shooter, this.cgc$shooterProperty.isBolting);
        LivingShooterSyncKey.RELOAD_STATE_KEY.setValue(cgc$shooter, reloadState);
        LivingShooterSyncKey.AIMING_PROGRESS_KEY.setValue(cgc$shooter, this.cgc$shooterProperty.aimingProgress);
        LivingShooterSyncKey.IS_AIMING_KEY.setValue(cgc$shooter, this.cgc$shooterProperty.isAiming);
        LivingShooterSyncKey.SPRINT_TIME_KEY.setValue(cgc$shooter, this.cgc$shooterProperty.sprintTimeS);
    }

    // --------ILivingShooter--------

    @Override
    public void cgc$initLivingShooter() {
        // 初始化ShooterProperty
        this.cgc$shooterProperty.resetProperty();
        // 主手武器
        this.cgc$shooterProperty.currentGunItem = this.cgc$shooter::getMainHandItem;
        // 刷新属性缓存
        ShooterGunModifierManager.postChangeEvent(cgc$shooter);
    }

    @Override public ShooterProperty cgc$getShooterProperty() {
        return this.cgc$shooterProperty;
    }

    @Override public boolean cgc$nextBulletIsTracer(int tracerCountInterval) {
        this.cgc$shooterProperty.shootCount++;
        if (tracerCountInterval < 0) {
            return false;
        }
        return this.cgc$shooterProperty.shootCount % (tracerCountInterval + 1) == 0;
    }

    // --------IGunOperator--------

    @Override
    public void cgc$prone(boolean isProne) {
        this.cgc$prone.prone(isProne);
    }

    @Override public void cgc$draw(Supplier<ItemStack> gunItemSupplier) {
        this.cgc$draw.draw(gunItemSupplier);
    }

    @Override
    public void cgc$switchFireMode() {
        this.cgc$switchFireMode.switchFireMode();
    }

    @Override
    public void cgc$aim(boolean isAim) {
        this.cgc$aim.aim(isAim);
    }
    @Override public void cgc$zoom() {
        this.cgc$aim.zoom();
    }

    @Override
    public void cgc$melee() {
        this.cgc$melee.prepareMelee();
    }

    @Override public ShootResult cgc$shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress) {
        return this.cgc$shoot.shoot(pitch, yaw, timestamp, chargeProgress);
    }
    @Override public ShootResult cgc$shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp) {
        return this.cgc$shoot.shoot(pitch, yaw, timestamp);
    }
    @Override public ShootResult cgc$shoot(Supplier<Float> pitch, Supplier<Float> yaw) {
        return this.cgc$shoot(pitch, yaw, System.currentTimeMillis() - cgc$shooterProperty.baseTimestamp);
    }

    @Override
    public void cgc$bolt() {
        this.cgc$bolt.bolt();
    }

    @Override
    public void cgc$reload() {
        this.cgc$reload.reload();
    }
    @Override public void cgc$cancelReload() {
        this.cgc$reload.cancelReload();
    }

    // --------IShooterState--------

    @Override
    public boolean cgc$needCheckAmmo() {
        return this.cgc$ammoCheck.needCheckAmmo();
    }

    @Override
    public boolean cgc$consumesAmmoOrNot() {
        return this.cgc$ammoCheck.consumesAmmoOrNot();
    }

    @Override
    public boolean cgc$getProcessedSprintStatus(boolean sprint) {
        return this.cgc$sprint.getProcessedSprintStatus(sprint);
    }

    @Override
    public ShootState cgc$getShootState() {
        return ShootState.fromLivingShooter(this, this.cgc$shooter);
    }

    // --------ISynGunState--------

    @Override public long cgc$getSynShootCooldown() {
        return LivingShooterSyncKey.SHOOT_COOL_DOWN_KEY.getValue(cgc$shooter);
    }
    @Override public long cgc$getSynMeleeCooldown() {
        return LivingShooterSyncKey.MELEE_COOL_DOWN_KEY.getValue(cgc$shooter);
    }
    @Override public long cgc$getSynDrawCooldown() {
        return LivingShooterSyncKey.DRAW_COOL_DOWN_KEY.getValue(cgc$shooter);
    }
    @Override public boolean cgc$getSynIsBolting() {
        return LivingShooterSyncKey.IS_BOLTING_KEY.getValue(cgc$shooter);
    }
    @Override public ReloadState cgc$getSynReloadState() {
        return LivingShooterSyncKey.RELOAD_STATE_KEY.getValue(cgc$shooter);
    }
    @Override public float cgc$getSynAimingProgress() {
        return LivingShooterSyncKey.AIMING_PROGRESS_KEY.getValue(cgc$shooter);
    }
    @Override public boolean cgc$getSynIsAiming() {
        return LivingShooterSyncKey.IS_AIMING_KEY.getValue(cgc$shooter);
    }
    @Override public float cgc$getSynSprintTime() {
        return LivingShooterSyncKey.SPRINT_TIME_KEY.getValue(cgc$shooter);
    }
    
    // --------IGunCacheHolder--------
    
    @Override public void cgc$updateGunModifierCache(ShooterGunModifierCache propertyCache) {
        this.cgc$shooterProperty.shooterGunModifierCache = propertyCache;
    }
    @Override public @Nullable ShooterGunModifierCache cgc$getGunModifierCache() {
        return this.cgc$shooterProperty.shooterGunModifierCache;
    }

    // --------IBulletVictimImpact--------

    @Override public boolean cgc$onProjectileImpact(IProjectilePhysicsRuntime.EntityHitResult entityHitResult,
                                                    IGunProjectile iGunProjectile, Entity gunProjectile) {
        LivingEntity victimEntity = this.cgc$shooter; // entityHitResult.entity();
        Vec3 hitPos = entityHitResult.hitPos();
        boolean headshot = entityHitResult.headshot();
        return ProjectileImpactManager.cgc$onProjectileImpact(entityHitResult, iGunProjectile, gunProjectile);
    }

    // --------IBulletVictimKnockback--------

    @Override public void cgc$resetKnockbackStrength() {
        cgc$knockbackStrength = -1;
    }
    @Override public float cgc$getKnockbackStrength() {
        return cgc$knockbackStrength;
    }
    @Override public void cgc$setKnockbackStrength(float strength) {
        this.cgc$knockbackStrength = strength;
    }
}
