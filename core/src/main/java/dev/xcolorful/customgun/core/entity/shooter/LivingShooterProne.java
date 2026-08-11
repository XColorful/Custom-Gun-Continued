/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.config.SyncConfig;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class LivingShooterProne extends LivingShooterAspect {

    public static final Pose PRONE_POSE = Pose.SWIMMING;

    /**
     * 是否处于趴下状态
     */
    private boolean isProne = false;
    /**
     * 是否设置过forcedPose，防止覆盖别的模组的操作
     */
    private boolean hasSetForcedPose = false;

    /**
     * 保持趴下的tick数，用于防止刚点趴姿，然后起跳时因为没达到{@link LivingShooterAspect#NOT_ON_GROUND_DISABLE_PRONE_TICKS}而没有立即取消
     */
    private int keepProneTicks = 0;
    /**
     * 不在地面的tick数，用来让下楼梯的时候不会立即解除趴姿
     */
    private int notOnGroundTicks = 0;

    public LivingShooterProne(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void prone(boolean isProne) {
        if (
                // 已经在空中就不能趴下，防止在刚离地到NOT_ON_GROUND_DISABLE_PRONE_TICKS期间趴下
                !this.livingShooter.onGround()
                // 禁止趴姿的状态
                || _shouldForceDisableProne()) {
            _setPronePose(false);
            return;
        }

        _setPronePose(isProne);
    }

    public void tickProne() {
        // tick计数
        if (this.isProne) this.keepProneTicks++;
        else this.keepProneTicks = 0;
        if (this.livingShooter.onGround()) this.notOnGroundTicks = 0;
        else this.notOnGroundTicks++;

        if ( // 2.2 检查状态
                // 悬空时间超时
                this.notOnGroundTicks >= NOT_ON_GROUND_DISABLE_PRONE_TICKS
                // 悬空时间没超时，但是趴下的时长不够，用来立即取消趴姿
                || this.keepProneTicks < PRONE_ANIMATION_TICKS && !this.livingShooter.onGround()
                // 禁止趴姿的状态
                || _shouldForceDisableProne()
                // 当前isProne，但是pose不对 -> 需更正状态 (用于双端同步)
                || (this.isProne && (this.livingShooter instanceof Player player) && player.getForcedPose() != PRONE_POSE)
        ) {
            this._setPronePose(false);
            return;
        }

        this._setPronePose(this.isProne);
    }

    @ApiStatus.Internal
    public boolean _shouldForceDisableProne() {
        if (isIllegalProneState(this.livingShooter)) {
            return true;
        }

        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return false;
        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        @Nullable IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return false;

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        // 不允许趴下的武器
        return !gunData.getEnableProne();
    }
    /**
     * 用于排除一定不允许趴姿的情况
     * @return 是否不可能(是否禁止)处于趴姿状态
     */
    @ApiStatus.Internal
    public static boolean isIllegalProneState(LivingEntity livingShooter) {
        if ( // 2.2 检查状态
                // 骑乘
                livingShooter.isPassenger()
                // 游泳
                || livingShooter.isSwimming()
                // 旁观模式
                || livingShooter.isSpectator()
                // 没开配置
                || !SyncConfig.ENABLE_PRONE.get()
        ) return true;
        return false;
    }

    private void _setPronePose(boolean isProne) {
        this.isProne = isProne;
        if (isProne) {
            if (this.livingShooter instanceof Player player) {
                player.setForcedPose(PRONE_POSE);
                this.hasSetForcedPose = true;
            } else this.livingShooter.setPose(PRONE_POSE);
        } else {
            if (this.hasSetForcedPose) {
                if (this.livingShooter instanceof Player player) player.setForcedPose(null);
                this.hasSetForcedPose = false;
            }
        }
    }
}
