/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.attachment.AttachmentCategory;
import xiao.customgun.core.api.item.attachment.AttachmentNBTAccessor;
import xiao.customgun.core.api.item.gun.IGunGetter;
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
        this.shooterProperty.isAiming = isAim;
    }

    public void zoom() {
        if (this.shooterProperty.currentGunItem == null) return;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        final var SCOPE = AttachmentCategory.SCOPE;
        @Nullable CompoundTag scopeCustomDataTag = iGun.getAttachmentCustomDataTag(currentGunItem, SCOPE);
        if (scopeCustomDataTag == null) return;

        var scopeLocation = iGun.getAttachmentLocation(currentGunItem, SCOPE);
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

        iGun.setAttachmentCustomDataTag(currentGunItem, SCOPE, scopeCustomDataTag);
    }

    @ApiStatus.Internal private void _resetAiming() {
        this.shooterProperty.aimingProgress = 0;
        this.shooterProperty.aimingTimestamp = System.currentTimeMillis();
    }
    public void tickAimingProgress() {
        if (this.shooterProperty.currentGunItem == null) {
            _resetAiming();
            return;
        }

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) {
            _resetAiming();
            return;
        }

        var gunLocation = iGun.getGunLocation(currentGunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) {
            // 获取不到data就取消瞄准状态并重置瞄准进度
            this.shooterProperty.aimingProgress = 0;
            return;
        }

        GunData gunData = gunIndexInstance.getGunData();
        float aimTime = gunData.getAimTime();
        if (this.shooterProperty.shooterGunPropertyCache != null) {
            // TODO GunPropertyCache
        }

        long currentTimeMillis = System.currentTimeMillis();
        if (aimTime <= 0) {
            this.shooterProperty.aimingProgress = this.shooterProperty.isAiming ? 1 : 0;
            this.shooterProperty.aimingTimestamp = currentTimeMillis;
            return;
        }
        float alphaProgress = (currentTimeMillis - this.shooterProperty.aimingTimestamp + 1) / (aimTime * 1000f);

        if (this.shooterProperty.isAiming) {
            // 瞄准状态加aimingProgress
            this.shooterProperty.aimingProgress += alphaProgress;
            if (this.shooterProperty.aimingProgress > 1) this.shooterProperty.aimingProgress = 1;
        } else {
            // 取消瞄准状态减aimingProgress
            this.shooterProperty.aimingProgress -= alphaProgress;
            if (this.shooterProperty.aimingProgress < 0) this.shooterProperty.aimingProgress = 0;
        }

        this.shooterProperty.aimingTimestamp = currentTimeMillis;
    }

    public void tickSprint() {
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        ReloadState reloadState = iLivingShooter.cgc$getSynReloadState();

        // 瞄准或换弹阶段 禁止疾跑
        if (this.shooterProperty.isAiming || (reloadState.getStateType().isReloading() && !reloadState.getStateType().isReloadFinishing())) {
            this.livingShooter.setSprinting(false);
        }

        long currentTimeMillis = System.currentTimeMillis();
        if (this.shooterProperty.sprintTimestamp < 0) {
            this.shooterProperty.sprintTimestamp = currentTimeMillis;
        }

        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance != null) {
            float gunSprintTime = gunIndexInstance.getGunData().getSprintTime();
            float alphaGunSprintTime = (currentTimeMillis - this.shooterProperty.sprintTimestamp) / 1000f;
            if (this.livingShooter.isSprinting() && !this.livingShooter.isCrouching()) {
                this.shooterProperty.sprintTimeS += alphaGunSprintTime;
                if (this.shooterProperty.sprintTimeS > gunSprintTime) this.shooterProperty.sprintTimeS = gunSprintTime;
            } else {
                this.shooterProperty.sprintTimeS -= alphaGunSprintTime;
                if (this.shooterProperty.sprintTimeS < 0) this.shooterProperty.sprintTimeS = 0;
            }
        } else {
            this.shooterProperty.sprintTimeS = 0;
        }

        this.shooterProperty.sprintTimestamp = currentTimeMillis;
    }
}
