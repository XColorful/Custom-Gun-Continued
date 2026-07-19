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
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.resource.instance.data.ClientGunIndexInstance;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.network.message.ClientMessagePlayerAim;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.SendUtils;

public final class LocalShooterAim extends LocalShooterAspect {

    public LocalShooterAim(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public void aim(boolean isAim) {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable ClientGunIndexInstance clientGunIndexInstance = ClientResourceApi.getClientGunIndexInstance(gunLocation);
        if (clientGunIndexInstance == null) return;

        this.localShooterProperty.clientIsAiming = isAim;
        SendUtils.sendMessageToServer(new ClientMessagePlayerAim(isAim));
    }

    public float getClientAimingProgress(float partialTicks) {
        return Mth.lerp(partialTicks, LocalShooterProperty.oldAimingProgress, this.localShooterProperty.clientAimingProgress);
    }

    public boolean isAim() {
        return this.localShooterProperty.clientIsAiming;
    }

    public void tickAimingProgress() {
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            // 主手不是枪械 -> 取消瞄准状态并重置aimingProgress
            this.localShooterProperty.clientAimingProgress = 0;
            LocalShooterProperty.oldAimingProgress = 0;
            return;
        }

        long currentTimeMillis = System.currentTimeMillis();

        // 正在收枪时不能瞄准
        if (currentTimeMillis - this.localShooterProperty.clientDrawTimestamp < 0) {
            this.localShooterProperty.clientIsAiming = false;
        }

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance != null) {
            float alphaProgress = _getAlphaProgress(gunIndexInstance.getGunData(), currentTimeMillis);
            _aimProgressCalculate(alphaProgress, currentTimeMillis);
        } else {
            this.localShooterProperty.clientAimingProgress = 0;
            LocalShooterProperty.oldAimingProgress = 0;
        }
    }
    private float _getAlphaProgress(GunData gunData, long currentTimeMillis) {
        float aimTime = gunData.getAimTime();
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.localShooter);
        if (iLivingShooter.cgc$getGunPropertyCache() != null) {
            // TODO GunPropertyCache AdsModifier
        }
        aimTime = Math.max(0, aimTime);
        return (currentTimeMillis - this.localShooterProperty.clientAimingTimestamp + 1) / (aimTime * 1000);
    }
    private void _aimProgressCalculate(float alphaProgress, long currentTimeMillis) {
        LocalShooterProperty.oldAimingProgress = this.localShooterProperty.clientAimingProgress;
        if (this.localShooterProperty.clientIsAiming) {
            // 处于执行瞄准状态，增加 aimingProgress
            this.localShooterProperty.clientAimingProgress += alphaProgress;
            if (this.localShooterProperty.clientAimingProgress > 1) {
                this.localShooterProperty.clientAimingProgress = 1;
            }
        } else {
            // 处于取消瞄准状态，减小 aimingProgress
            this.localShooterProperty.clientAimingProgress -= alphaProgress;
            if (this.localShooterProperty.clientAimingProgress < 0) {
                this.localShooterProperty.clientAimingProgress = 0;
            }
        }
        this.localShooterProperty.clientAimingTimestamp = currentTimeMillis;
    }
}
