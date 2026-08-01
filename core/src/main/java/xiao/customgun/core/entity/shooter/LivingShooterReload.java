/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.network.message.event.ServerMessageGunReload;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.SendUtils;

public final class LivingShooterReload extends LivingShooterAspect {

    private final LivingShooterDraw draw;
    private final LivingShooterShoot shoot;

    public LivingShooterReload(LivingEntity livingShooter, ShooterProperty shooterProperty,
                               LivingShooterDraw draw, LivingShooterShoot shoot) {
        super(livingShooter, shooterProperty);
        this.draw = draw;
        this.shoot = shoot;
    }

    public void reload() {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        long currentTimeMillis = System.currentTimeMillis();
        if ( // 2.1 检查状态锁
                // 检查换弹是否还未完成
                this.shooterProperty.reloadStateType.isReloading()
                // 检查是否在拉栓
                || this.shooterProperty.isBolting
                // 检查是否正在开火冷却
                || this.shoot.getShootCooldown() > 0
                // 检查是否在切枪
                || this.draw.getDrawCooldown() > 0
        ) return;
        else if ( // 2.2
                // 射击后冷却50ms (比客户端快一点)
                currentTimeMillis - this.shooterProperty.lastShootTimestamp < RELOAD_COOLDOWN_MS
        ) return;

        { // 3. IGunRuntime操作结果 -> Shooter状态
            ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
            boolean isReloading = iGun.startReload(this.shooterProperty, iGun, gunItem, iLivingShooter, this.livingShooter);
            if (!isReloading) {
                this.shooterProperty.reloadStateType = ReloadState.StateType.NOT_RELOADING;
                this.shooterProperty.reloadTimestamp = -1;
                return;
            }
        }

        // 发包通知客户端
        SendUtils.sendMessageToTrackingEntity(this.livingShooter,
                new ServerMessageGunReload(this.livingShooter.getId(), gunItem));

        // 执行服务端 reload 相关内容
        this._doReload(currentTimeMillis, iGun, gunItem);
    }
    private void _doReload(long currentTimeMillis, IGun iGun, ItemStack gunItem) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;

        GunData gunData = gunIndexInstance.getGunData();
        BoltType boltType = gunData.getBoltType();
        if (boltType == BoltType.CLOSED_BOLT && iGun.hasBarrelAmmo(gunItem)) {
            // 初始化战术换弹的 tick 的状态
            this.shooterProperty.reloadStateType = ReloadState.StateType.TACTICAL_RELOAD_FEEDING;
        } else {
            // 初始化空仓换弹的 tick 的状态
            this.shooterProperty.reloadStateType = ReloadState.StateType.EMPTY_RELOAD_FEEDING;
        }
        this.shooterProperty.reloadTimestamp = currentTimeMillis;
    }

    public ReloadState tickReloadState() {
        ReloadState result = new ReloadState();

        // 不在换弹
        if (this.shooterProperty.reloadTimestamp < 0) {
            return result;
        }

        // 调用枪械逻辑
        if (this.shooterProperty.currentGunItem != null) {
            ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
            if (currentGunItem != null) {
                IGun iGun = IGunGetter.fromItemStack(currentGunItem);
                if (iGun != null) {
                    result = iGun.tickReload(this.shooterProperty, iGun, currentGunItem, ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter), this.livingShooter);
                }
            }
        }

        // 将 tick 的结果保存到 shooterProperty
        this.shooterProperty.reloadStateType = result.getStateType();
        if (!result.getStateType().isReloading()) {
            this.shooterProperty.reloadTimestamp = -1;
        }
        return result;
    }

    public void cancelReload() {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        else if ( // 2.2
                // 检查是否在换弹
                !this.shooterProperty.reloadStateType.isReloading()
        ) return;

        // 3. IGunRuntime操作结果 -> Shooter状态
        iGun.interruptReload(this.shooterProperty, iGun, currentGunItem, ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter), this.livingShooter);
    }
}
