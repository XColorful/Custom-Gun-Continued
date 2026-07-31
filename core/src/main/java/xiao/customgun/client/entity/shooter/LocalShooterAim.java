/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.entity.shooter.LivingShooterAim;
import xiao.customgun.core.network.message.ClientMessagePlayerAim;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterAim extends LocalShooterAspect {

    public LocalShooterAim(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void aim(boolean isAim) {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        this.localShooterProperty.clientIsAiming = isAim;

        SendUtils.sendMessageToServer(new ClientMessagePlayerAim(isAim));
    }

    public float getRenderAimingProgress(float partialTicks) {
        // TODO 非线性映射
        return Mth.lerp(partialTicks, LocalShooterProperty.oldAimingProgress, this.localShooterProperty.clientAimingProgress);
    }

    public boolean isAim() {
        return this.localShooterProperty.clientIsAiming;
    }

    @ApiStatus.Internal private void _resetAiming() {
        this.localShooterProperty.clientIsAiming = false;
        this.localShooterProperty.clientAimingProgress = 0;
        LocalShooterProperty.oldAimingProgress = System.currentTimeMillis();
    }
    public void tickAimingProgress() {
        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            _resetAiming();
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();
        if ( // 2.2
                // 正在收枪时不能瞄准
                currentTimeMillis < this.localShooterProperty.clientDrawFinishTimestamp
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
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        float alphaProgress = LivingShooterAim._getAlphaProgress(iLivingShooter, gunData, this.localShooterProperty.clientIsAiming, currentTimeMillis, this.localShooterProperty.clientAimingTimestamp);

        _aimProgressCalculate(this.localShooterProperty.clientIsAiming, alphaProgress, currentTimeMillis);
    }
    private void _aimProgressCalculate(boolean isAiming,
                                       float alphaProgress, long currentTimeMillis) {
        LocalShooterProperty.oldAimingProgress = this.localShooterProperty.clientAimingProgress;

        float aimProgress = this.localShooterProperty.clientAimingProgress + (isAiming ? alphaProgress : -alphaProgress);
        this.localShooterProperty.clientAimingProgress = Mth.clamp(aimProgress, 0, 1f);

        this.localShooterProperty.clientAimingTimestamp = currentTimeMillis;
    }
}
