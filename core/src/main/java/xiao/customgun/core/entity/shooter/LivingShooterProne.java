/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.shooter;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

public final class LivingShooterProne extends LivingShooterAspect {

    public LivingShooterProne(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void prone(boolean isProne) {
        this.shooterProperty.isProne = isProne;
    }

    public void tickProne() {
        if (this.shooterProperty.currentGunItem == null) {
            this.shooterProperty.isProne = false;
            this.setPronePose();
            return;
        }

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) {
            this.shooterProperty.isProne = false;
            this.setPronePose();
            return;
        }

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null
                || !gunIndexInstance.getGunData().getEnableProne() // 不允许趴下的武器
                || !this.livingShooter.onGround() // 悬空
                || this.livingShooter.isPassenger() // 骑乘
                || this.livingShooter.isSwimming() // 游泳
                || this.livingShooter.isSpectator() // 旁观模式
        ) {
            this.shooterProperty.isProne = false;
        }

        this.setPronePose();
    }

    private void setPronePose() {
        if (this.shooterProperty.isProne) {
            if (this.livingShooter instanceof Player player) player.setForcedPose(Pose.SWIMMING);
            else this.livingShooter.setPose(Pose.SWIMMING);
        } else {
            if (this.livingShooter instanceof Player player) player.setForcedPose(null);
        }
    }
}
