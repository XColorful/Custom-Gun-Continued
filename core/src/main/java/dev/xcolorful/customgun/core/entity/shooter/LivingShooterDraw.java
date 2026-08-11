/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.entity.shooter;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.event.shooter.ShooterDrawEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.entity.shooter.modifier.ShooterGunModifierManager;
import dev.xcolorful.customgun.core.network.message.event.ServerMessageGunDraw;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import dev.xcolorful.customgun.core.util.SendUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class LivingShooterDraw extends LivingShooterAspect {

    /**
     * 缓存当前枪械的收枪时间，使下一次切枪的时候用此时间计算收枪
     */
    private float currentPutAwayTimeS = 0;

    public LivingShooterDraw(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void draw(Supplier<ItemStack> gunItemSupplier) {
        // 重置各个状态
        long currentTimeMillis = System.currentTimeMillis();
        this.shooterProperty.resetProperty();
        // 初始化切枪时间戳
        if (this.shooterProperty.drawFinishTimestamp < 0) this.shooterProperty.drawFinishTimestamp = currentTimeMillis;

        // 初始化热量时间戳
        if (this.shooterProperty.heatTimestamp < 0) this.shooterProperty.heatTimestamp = currentTimeMillis;

        // 更新切枪结束时间
        long fromLastDrawFinishedMs = currentTimeMillis - this.shooterProperty.drawFinishTimestamp;
        if (fromLastDrawFinishedMs >= 0) { // draw结束 在 当前时间 之前 -> 当前没有一个未来的drawFinishTime -> 当前不在draw
            this.shooterProperty.drawFinishTimestamp = _calculateDrawFinishTime(fromLastDrawFinishedMs, currentTimeMillis);
        }

        ItemStack lastItem = this.shooterProperty.currentGunItem != null ? this.shooterProperty.currentGunItem.get()
                : ItemStack.EMPTY;
        ILivingShooter iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(this.livingShooter);
        CustomGun.getEventPoster().postCustomEvent(new ShooterDrawEvent(McLogicalSide.SERVER,
                iLivingShooter, this.livingShooter, lastItem, gunItemSupplier.get()));

        SendUtils.sendMessageToTrackingEntity(this.livingShooter,
                new ServerMessageGunDraw(this.livingShooter.getId(), lastItem, gunItemSupplier.get()));

        this.shooterProperty.currentGunItem = gunItemSupplier;

        // 刷新配件数据
        ShooterGunModifierManager.postChangeEvent(this.livingShooter, gunItemSupplier.get());

        this.currentPutAwayTimeS = _calculatePutAwayTime();
    }

    /**
     * 不处于收枪状态时，计算收枪时长
     */
    private long _calculateDrawFinishTime(long fromLastDrawFinishedMs, long currentTimeMillis) {
        long putAwayTimeMs = (long) (this.currentPutAwayTimeS * 1000);
        if (fromLastDrawFinishedMs < putAwayTimeMs) {
            // 从开始切枪到现在，抬枪的时间小于收枪需要的时间 -> 按抬枪时间计算
            return currentTimeMillis + fromLastDrawFinishedMs;
        } else {
            // 从开始切枪到现在，抬枪的时间大于收枪需要的时间 -> 按收枪时间计算
            return currentTimeMillis + putAwayTimeMs;
        }
    }

    private float _calculatePutAwayTime() {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return 0;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return 0;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        return gunData.getPutAwayTime();
    }

    /**
     * @return 0 -> 无冷却; -1 -> 无数据
     */
    public long getDrawCooldown() {
        // 1. 手持枪械检查
        if (this.shooterProperty.currentGunItem == null) return 0;
        ItemStack gunItem = this.shooterProperty.currentGunItem.get();
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return 0;

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return -1;

        GunData gunData = gunIndexInstance.getGunData();
        long gunDrawTimeMs = (long) (gunData.getDrawTime() * 1000);
        long fromLastDrawFinishedMs = System.currentTimeMillis() - this.shooterProperty.drawFinishTimestamp;

        long coolDown = gunDrawTimeMs - fromLastDrawFinishedMs;
        // 给 5 ms 的窗口时间，以平衡延迟
        return coolDown < WINDOW_TIME_MS ? 0 : coolDown;
    }
}
