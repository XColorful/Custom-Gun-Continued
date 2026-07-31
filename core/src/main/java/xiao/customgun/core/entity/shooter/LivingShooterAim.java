/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.item.attachment.AttachmentNBTAccessor;
import xiao.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.item.gun.modifier.IAdsModifier;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.developer.PlannedRefactor;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.AttachmentIndexInstance;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

public final class LivingShooterAim extends LivingShooterAspect {

    public LivingShooterAim(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void aim(boolean isAim) {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            this.shooterProperty.isAiming = false;
            return;
        }

        this.shooterProperty.isAiming = isAim;
    }

    @ApiStatus.Internal private void _resetAiming() {
        this.shooterProperty.aimingProgress = 0;
        this.shooterProperty.aimingTimestamp = System.currentTimeMillis();
    }
    public void tickAimingProgress() {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) {
            _resetAiming();
            return;
        }
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            _resetAiming();
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        if ( // 2.2
                // 正在收枪时不能瞄准
                currentTimeMillis < this.shooterProperty.drawFinishTimestamp
        ) {
            _resetAiming();
            return;
        }

        _doAiming(iGun, gunItem, currentTimeMillis);
    }
    private void _doAiming(IGun iGun, ItemStack gunItem, long currentTimeMillis) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) {
            _resetAiming();
            return;
        }

        GunData gunData = gunIndexInstance.getGunData();
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        float alphaProgress = LivingShooterAim._getAlphaProgress(iLivingShooter, gunData, this.shooterProperty.isAiming, currentTimeMillis, this.shooterProperty.aimingTimestamp);

        _aimProgressCalculate(this.shooterProperty.isAiming, alphaProgress, currentTimeMillis);
    }
    @ApiStatus.Internal
    public static float _getAlphaProgress(ILivingShooter iLivingShooter,
                                          GunData gunData,
                                          boolean isAiming,
                                          long currentTimeMillis, long aimingTimestamp) {
        float aimTime = gunData.getAimTime();
        if (aimTime <= 0) {
            return isAiming ? 1 : 0;
        }

        @Nullable ShooterGunModifierCache cache = iLivingShooter.cgc$getGunModifierCache();
        if (cache != null) {
            Float _aimTime = IAdsModifier.getValue(cache, AttachmentModifierType.ADS);
            if (_aimTime != null) aimTime = _aimTime;
        }

        return (currentTimeMillis - aimingTimestamp + 1) / (aimTime * 1000);
    }
    private void _aimProgressCalculate(boolean isAiming,
                                       float alphaProgress, long currentTimeMillis) {
        float aimProgress = this.shooterProperty.aimingProgress + (isAiming ? alphaProgress : -alphaProgress);
        this.shooterProperty.aimingProgress = Mth.clamp(aimProgress, 0, 1f);

        this.shooterProperty.aimingTimestamp = currentTimeMillis;
    }

    public void zoom() {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        _doZoom(iGun, gunItem);
    }
    private void _doZoom(IGun iGun, ItemStack gunItem) {
        final var SCOPE = AttachmentCategory.SCOPE;
        @Nullable CompoundTag scopeCustomDataTag = iGun.getAttachmentCustomDataTag(gunItem, SCOPE);
        if (scopeCustomDataTag == null) return;

        var scopeLocation = iGun.getAttachmentLocation(gunItem, SCOPE);
        @Nullable AttachmentIndexInstance attachmentIndexInstance = ResourceApi.getAttachmentIndexInstance(scopeLocation);
        if (attachmentIndexInstance == null) return;

        int scopeViewIndex = AttachmentNBTAccessor.INSTANCE.getScopeViewIndex(scopeCustomDataTag);
        scopeViewIndex++;
//        zoomNumber = zoomNumber % (Integer.MAX_VALUE - 1); // 取模不能避免变负，原模组的注释有误
        if (scopeViewIndex < 0) {
            if (PlannedRefactor.MOVE_SCOPE_VIEW_INDEX_TO_CORE) {}
            scopeViewIndex = 0;
        }
        AttachmentNBTAccessor.INSTANCE.setScopeViewIndex(scopeCustomDataTag, scopeViewIndex);

        iGun.setAttachmentCustomDataTag(gunItem, SCOPE, scopeCustomDataTag);
    }

    /**
     * 上次执行{@link #tickSprint()}的时间戳，用于计算疾跑时间变化量
     */
    private long lastSprintTickTimestamp = -1;
    public void tickSprint() {
        long currentTimeMillis = System.currentTimeMillis();

        // 初始化时间戳
        if (this.lastSprintTickTimestamp < 0) this.lastSprintTickTimestamp = currentTimeMillis;

        _doTickSprint(currentTimeMillis);

        // 设置sprint时间戳
        this.lastSprintTickTimestamp = currentTimeMillis;
    }
    private void _doTickSprint(long currentTimeMillis) {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        ReloadState reloadState = iLivingShooter.cgc$getSynReloadState();
        if ( // 2.2 禁止疾跑
            // 瞄准状态
                this.shooterProperty.aimingProgress > 0
                        // 换弹中
                        || (reloadState.getStateType().isReloading() && !reloadState.getStateType().isReloadFinishing())
        ) {
            this.livingShooter.setSprinting(false);
        }

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null) {
            this.shooterProperty.sprintTimeS = 0;
            return;
        }

        // 当前tick时间戳-上次tick时间戳
        float alphaGunSprintTime = (currentTimeMillis - this.lastSprintTickTimestamp) / 1000f;
        boolean isSprinting = this.livingShooter.isSprinting() && !this.livingShooter.isCrouching();
        float sprintTimeS = this.shooterProperty.sprintTimeS + (isSprinting ? alphaGunSprintTime : -alphaGunSprintTime);

        GunData gunData = gunIndexInstance.getGunData();
        float gunSprintSwitchTime = gunData.getSprintSwitchTime();
        this.shooterProperty.sprintTimeS = Mth.clamp(sprintTimeS, 0, gunSprintSwitchTime);
    }
}
