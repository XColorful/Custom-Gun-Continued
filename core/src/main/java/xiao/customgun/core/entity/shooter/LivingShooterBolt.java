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
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

public final class LivingShooterBolt extends LivingShooterAspect {

    private final LivingShooterDraw draw;
    private final LivingShooterShoot shoot;

    public LivingShooterBolt(LivingEntity livingShooter, ShooterProperty shooterProperty,
                             LivingShooterDraw draw, LivingShooterShoot shoot) {
        super(livingShooter, shooterProperty);
        this.draw = draw;
        this.shoot = shoot;
    }

    public void bolt() {
        if (this.shooterProperty.currentGunItem == null) return;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return;

        // 缓存近的判断前置
        if (
                // 判断是否正在射击冷却
                this.shoot.getShootCooldown() > 0
                // 检查是否正在换弹
                || this.shooterProperty.reloadStateType.isReloading()
                // 检查是否在切枪
                || this.draw.getDrawCooldown() > 0
                // 检查是否在拉栓
                || this.shooterProperty.isBolting
                // 检查是否有弹药在枪膛内
                || iGun.hasBarrelAmmo(currentGunItem)
        ) return;

        var gunLocation = iGun.getGunLocation(currentGunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return;

        // 检查 bolt 类型是否是 manual action
        BoltType boltType = gunIndexInstance.getGunData().getBoltType();
        if (boltType != BoltType.MANUAL_ACTION) return;


        // 判断没有子弹的条件 (背包直读且包内没子弹 / 非背包直读且弹匣子弹数 < 1)
        boolean useInventoryAmmo = iGun.useInventoryAmmo(currentGunItem); // 是否为背包直读
        boolean hasAmmo = useInventoryAmmo ? iGun.hasInventoryAmmo(this.livingShooter, currentGunItem)
                : iGun.getMagAmmoCount(currentGunItem) < 1;
        if (!hasAmmo) return;

        this.shooterProperty.boltTimestamp = System.currentTimeMillis();
        this.shooterProperty.isBolting = iGun.startBolt(this.shooterProperty, iGun, currentGunItem, ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter), this.livingShooter);
    }

    public void tickBolt() {
        if (!this.shooterProperty.isBolting) return; // 拉栓逻辑进程没有开始

        if (this.shooterProperty.currentGunItem == null) {
            this.shooterProperty.isBolting = false;
            return;
        }

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) {
            this.shooterProperty.isBolting = false;
            return;
        }

        var gunLocation = iGun.getGunLocation(currentGunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        this.shooterProperty.isBolting = gunIndexInstance != null
                && iGun.tickBolt(this.shooterProperty, iGun, currentGunItem, ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter), this.livingShooter);
    }
}
