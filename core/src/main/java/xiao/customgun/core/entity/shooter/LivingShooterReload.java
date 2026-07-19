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
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.shooter.ShooterReloadEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.event.EventPoster;
import xiao.customgun.core.network.message.event.ServerMessageGunReload;
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
        if (this.shooterProperty.currentGunItem == null) return;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null) return;

        if (this.shooterProperty.reloadStateType.isReloading() // 检查换弹是否还未完成
                || this.shooterProperty.isBolting // 检查是否在拉栓
                || this.shoot.getShootCooldown() != 0 // 检查是否正在开火冷却
                || this.draw.getDrawCooldown() != 0 // 检查是否在切枪
                || iGun.useInventoryAmmo(currentGunItem) // 检查是否为背包直读
        ) return;

        // 检查弹药
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        if (iLivingShooter.cgc$needCheckAmmo() && !iGun.canReload(currentGunItem, this.livingShooter)) return;

        // 触发装弹事件
        if (EventPoster.get().postCustomEvent(new ShooterReloadEvent(McLogicalSide.SERVER,
                iLivingShooter, this.livingShooter, iGun, currentGunItem))) {
            return;
        }
        SendUtils.sendMessageToTrackingEntity(this.livingShooter,
                new ServerMessageGunReload(this.livingShooter.getId(), currentGunItem));

        BoltType boltType = gunIndexInstance.getGunData().getBoltType();
        if (boltType == BoltType.CLOSED_BOLT && iGun.hasBarrelAmmo(currentGunItem)) {
            // 初始化战术换弹的 tick 的状态
            this.shooterProperty.reloadStateType = ReloadState.StateType.TACTICAL_RELOAD_FEEDING;
        } else {
            // 初始化空仓换弹的 tick 的状态
            this.shooterProperty.reloadStateType = ReloadState.StateType.EMPTY_RELOAD_FEEDING;
        }
        this.shooterProperty.reloadTimestamp = System.currentTimeMillis();

        // 调用枪械逻辑
        if (!iGun.startReload(this.shooterProperty, currentGunItem, this.livingShooter)) {
            this.shooterProperty.reloadStateType = ReloadState.StateType.NOT_RELOADING;
            this.shooterProperty.reloadTimestamp = -1;
        }
    }

    public void cancelReload() {
        if (this.shooterProperty.currentGunItem == null) return;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        // 检查是否在换弹
        if (!this.shooterProperty.reloadStateType.isReloading()) return;

        iGun.interruptReload(this.shooterProperty, currentGunItem, this.livingShooter);
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
                    result = iGun.tickReload(this.shooterProperty, currentGunItem, this.livingShooter);
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
}
