/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

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
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        if ( // 2.1 检查状态锁
                // 判断是否正在射击冷却
                this.shoot.getShootCooldown() > 0
                // 检查是否正在换弹
                || this.shooterProperty.reloadStateType.isReloading()
                // 检查是否在切枪
                || this.draw.getDrawCooldown() > 0
        ) return;
        else if ( // 2.2 检查状态
                // 检查是否在拉栓
                this.shooterProperty.isBolting
        ) return;

        { // 3. IGunRuntime操作结果 -> Shooter状态
            ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
            this.shooterProperty.isBolting = iGun.startBolt(this.shooterProperty, iGun, gunItem, iLivingShooter, this.livingShooter);
            if (!this.shooterProperty.isBolting) {
                return;
            }
        }

        this.shooterProperty.boltTimestamp = System.currentTimeMillis();
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
        if (gunIndexInstance == null) {
            this.shooterProperty.isBolting = false;
            return;
        }

        this.shooterProperty.isBolting = iGun.tickBolt(this.shooterProperty, iGun, currentGunItem, ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter), this.livingShooter);
    }
}
