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
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.entity.shooter.ILivingShooterGetter;
import xiao.customgun.core.api.event.shooter.ShooterDrawEvent;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.item.gun.GunPropertyManager;
import xiao.customgun.core.network.message.event.ServerMessageGunDraw;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;
import xiao.customgun.core.util.SendUtils;

import java.util.function.Supplier;

public final class LivingShooterDraw extends LivingShooterAspect {

    public LivingShooterDraw(LivingEntity livingShooter, ShooterProperty shooterProperty) {
        super(livingShooter, shooterProperty);
    }

    public void draw(Supplier<ItemStack> gunItemSupplier) {
        // 重置各个状态
        this.shooterProperty.resetProperty();

        long currentTimeMillis = System.currentTimeMillis();

        if (this.shooterProperty.drawTimestamp < 0) this.shooterProperty.drawTimestamp = currentTimeMillis;
        if (this.shooterProperty.heatTimestamp < 0) this.shooterProperty.heatTimestamp = currentTimeMillis;

        // 更新切枪时间戳
        long drawTime = currentTimeMillis - this.shooterProperty.drawTimestamp;
        if (drawTime >= 0) {
            // 不处于收枪状态时，计算收枪时长
            if (drawTime < this.shooterProperty.currentPutAwayTimeS * 1000) {
                // 从开始切枪到现在，抬枪的时间小于收枪需要的时间 -> 按抬枪时间计算
                this.shooterProperty.drawTimestamp = currentTimeMillis + drawTime;
            } else {
                // 从开始切枪到现在，抬枪的时间大于收枪需要的时间 -> 按收枪时间计算
                this.shooterProperty.drawTimestamp = currentTimeMillis + (long) (this.shooterProperty.currentPutAwayTimeS * 1000);
            }
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
        GunPropertyManager.postChangeEvent(this.livingShooter, gunItemSupplier.get());
        updatePutAwayTime();
    }

    /**
     * @return 0 -> 无冷却; -1 -> 无数据
     */
    public long getDrawCooldown() {
        if (this.shooterProperty.currentGunItem == null) return 0;

        ItemStack currentGunItem = this.shooterProperty.currentGunItem.get();
        IGun iGun = IGunGetter.fromItemStack(currentGunItem);
        if (iGun == null) return 0;

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(currentGunItem));
        if (gunIndexInstance == null) return -1;

        long coolDown = (long) (gunIndexInstance.getGunData().getDrawTime() * 1000)
                - (System.currentTimeMillis() - this.shooterProperty.drawTimestamp);
        // 给 5 ms 的窗口时间，以平衡延迟
        return coolDown < WINDOW_TIME_MS ? 0 : coolDown;
    }

    private void updatePutAwayTime() {
        ItemStack gunItem = this.shooterProperty.currentGunItem != null ? this.shooterProperty.currentGunItem.get()
                : ItemStack.EMPTY;
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) {
            this.shooterProperty.currentPutAwayTimeS = 0;
            return;
        }

        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(iGun.getGunLocation(gunItem));
        this.shooterProperty.currentPutAwayTimeS = gunIndexInstance != null ? gunIndexInstance.getGunData().getPutAwayTime() : 0F;
    }
}
