/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.entity.shooter;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.entity.LocalShooterProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.entity.shooter.LivingShooterAspect;
import xiao.customgun.core.entity.shooter.LivingShooterProne;
import xiao.customgun.core.network.message.ClientMessagePlayerProne;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.SendUtils;

import static xiao.customgun.core.entity.shooter.LivingShooterAspect.NOT_ON_GROUND_DISABLE_PRONE_TICKS;
import static xiao.customgun.core.entity.shooter.LivingShooterAspect.PRONE_ANIMATION_TICKS;

public final class LocalShooterProne extends LocalShooterAspect {

    private static final int COOLDOWN_TICKS = 10;
    /**
     * 纯客户端的按键冷却，服务端不检查
     */
    private int proneCooldownTicks = 0;

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

    public LocalShooterProne(LocalPlayer localShooter, LocalShooterProperty localShooterProperty) {
        super(localShooter, localShooterProperty);
    }

    public boolean isProne() {
        return this.isProne;
    }

    public void prone(boolean isProne) {
        if (
                // 已经在空中就不能趴下，防止在刚离地到NOT_ON_GROUND_DISABLE_PRONE_TICKS期间趴下
                !this.localShooter.onGround()
                // 禁止趴姿的状态
                || _shouldForceDisableProne()) {
            _setPronePose(false);
            return;
        }

        // (客户端)允许按键生效的冷却时间没到
        if (this.proneCooldownTicks > 0) return;
        else this.proneCooldownTicks = COOLDOWN_TICKS;

        _setPronePose(isProne);

        SendUtils.sendMessageToServer(new ClientMessagePlayerProne(isProne));
    }

    public void tickProne() {
        // tick计数
        if (this.isProne) this.keepProneTicks++;
        else this.keepProneTicks = 0;
        if (this.localShooter.onGround()) this.notOnGroundTicks = 0;
        else this.notOnGroundTicks++;

        if (this.proneCooldownTicks > 0) this.proneCooldownTicks--;

        if ( // 2.2 检查状态
                // 悬空时间超时
                this.notOnGroundTicks >= NOT_ON_GROUND_DISABLE_PRONE_TICKS
                // 悬空时间没超时，但是趴下的时长不够，用来立即取消趴姿
                || this.keepProneTicks < PRONE_ANIMATION_TICKS && !this.localShooter.onGround()
                // 禁止趴姿的状态
                || _shouldForceDisableProne()
                // 当前isProne，但是pose不对 -> 需更正状态 (用于双端同步)
                || (this.isProne && this.localShooter.getForcedPose() != LivingShooterProne.PRONE_POSE)
        ) {
            this._setPronePose(false);
            return;
        }

        this._setPronePose(this.isProne);
    }

    private boolean _shouldForceDisableProne() {
        if (LivingShooterProne.isIllegalProneState(this.localShooter)) {
            return true;
        }

        // 1. 手持枪械检查
        ItemStack gunItem = this.localShooter.getMainHandItem();
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return false;

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(gunItem));
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        // 不允许趴下的武器
        return !gunData.getEnableProne();
    }

    private void _setPronePose(boolean isProne) {
        this.isProne = isProne;
        if (isProne) {
            this.localShooter.setForcedPose(LivingShooterProne.PRONE_POSE);
            this.hasSetForcedPose = true;
        } else {
            if (this.hasSetForcedPose) {
                this.localShooter.setForcedPose(null);
                this.hasSetForcedPose = false;
            }
        }
    }
}
