/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.api.entity;

import net.minecraft.client.player.LocalPlayer;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;

import javax.annotation.Nullable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Predicate;

public class LocalShooterProperty {

    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(2);
    /**
     * 点击按钮的时间戳，防止客户端点击按钮后误触开火
     */
    public static long clientClickButtonTimestamp = -1L;

    private final LocalPlayer cgc$localPlayer;
    public LocalShooterProperty(LocalPlayer cgc$localPlayer) {
        this.cgc$localPlayer = cgc$localPlayer;
    }

    public long clientBaseTimestamp = -1L;
    /**
     * 上一个 tick 的瞄准进度，用于插值，范围 0 ~ 1
     */
    public float oldAimingProgress = 0;
    /**
     * 与射击有关的几个变量
     */
    public volatile long clientShootTimestamp = -1L;
    public volatile long clientLastShootTimestamp = -1L;
    public volatile boolean isShootRecorded = true;
    public float chargeProgress = 0f;
    public boolean isCharging = false;
    /**
     * 这个状态锁表示：任意时刻，正在进行的枪械操作只能为一个。
     * 主要用于防止客户端操作表现效果重复执行。
     */
    public volatile boolean clientStateLock = false;
    /**
     * 用于标记 bolt 是否已经执行完成，防止因为客户端、服务端异步产生的数据不同步而造成的重复 bolt
     */
    public boolean isBolting = false;
    /**
     * 瞄准的进度，范围 0 ~ 1
     */
    public float clientAimingProgress = 0;
    /**
     * 瞄准时间戳，单位 ms
     */
    public long clientAimingTimestamp = -1L;
    public boolean clientIsAiming = false;
    /**
     * 切枪时间戳，在切枪开始时更新，单位 ms。
     * 在客户端仅用于计算收枪动画的时长和过渡时长。
     */
    public long clientDrawTimestamp = -1L;
    /**
     * 异步切枪
     */
    @Nullable
    public ScheduledFuture<?> drawFuture = null;
    /**
     * 用于等待上锁的服务端响应
     */
    @Nullable
    public Predicate<ILivingShooter> lockedCondition = null;
    /**
     * 计算上锁响应时间，不允许超过最大响应时间，避免死锁
     */
    public long lockTimestamp = -1;

    /**
     * 锁上状态锁
     */
    public void lockState(@Nullable Predicate<ILivingShooter> lockedCondition) {
        clientStateLock = true;
        lockTimestamp = System.currentTimeMillis();
        this.lockedCondition = lockedCondition;
    }

    /**
     * 此方法每 tick 执行一次，判断是否应当释放状态锁。
     */
    public void tickStateLock() {
        ILivingShooter gunOperator = ILivingShooterGetter.cgc$fromLivingEntity(cgc$localPlayer);
        ReloadState reloadState = gunOperator.cgc$getSynReloadState();
        // 如果还没完成上锁，则不能释放状态锁
        // 上锁允许的最大响应时间，毫秒
        long maxLockTime = 250;
        long lockTime = System.currentTimeMillis() - lockTimestamp;
        if (lockTime < maxLockTime && lockedCondition != null && !lockedCondition.test(gunOperator)) {
            return;
        }
        lockedCondition = null;
        if (reloadState.getStateType().isReloading()) {
            return;
        }
        long shootCoolDown = gunOperator.cgc$getSynShootCooldown();
        if (shootCoolDown > 0) {
            return;
        }
        if (gunOperator.cgc$getSynDrawCooldown() > 0) {
            return;
        }
        if (gunOperator.cgc$getSynIsBolting()) {
            return;
        }
        if (gunOperator.cgc$getSynMeleeCooldown() > 0) {
            return;
        }
        // 释放状态锁
        clientStateLock = false;
    }

    /**
     * 重生后各种参数的重置
     */
    public void resetProperty() {
        // 重置客户端的 shoot 时间戳
        isShootRecorded = true;
        clientShootTimestamp = -1;
        chargeProgress = 0f;
        isCharging = false;
        // 重置客户端瞄准状态
        clientIsAiming = false;
        clientAimingProgress = 0;
        oldAimingProgress = 0;
        // 重置拉栓状态
        isBolting = false;
        // 打开状态锁
        clientStateLock = false;
    }
}
