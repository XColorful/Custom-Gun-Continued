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

public final class LivingShooterCrawl extends LivingShooterAspect {

    public LivingShooterCrawl(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void crawl(boolean isCrawl) {
        this.shooterProperty.isCrawling = isCrawl;
    }

    public void tickCrawling() {
        if (this.shooterProperty.currentGunItem == null) {
            this.shooterProperty.isCrawling = false;
            this.setCrawlPose();
            return;
        }

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) {
            this.shooterProperty.isCrawling = false;
            this.setCrawlPose();
            return;
        }

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null
                || !gunIndexInstance.getGunData().getEnableCrawl() // 不允许趴下的武器
                || !this.livingShooter.onGround() // 悬空
                || this.livingShooter.isPassenger() // 骑乘
                || this.livingShooter.isSwimming() // 游泳
                || this.livingShooter.isSpectator() // 旁观模式
        ) {
            this.shooterProperty.isCrawling = false;
        }

        this.setCrawlPose();
    }

    private void setCrawlPose() {
        if (this.shooterProperty.isCrawling) {
            if (this.livingShooter instanceof Player player) player.setForcedPose(Pose.SWIMMING);
            else this.livingShooter.setPose(Pose.SWIMMING);
        } else {
            if (this.livingShooter instanceof Player player) player.setForcedPose(null);
        }
    }
}
