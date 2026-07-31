/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.gun.action;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.CustomGun;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ReloadState;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.AmmoFeedType;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.item.gun.FireModeType;
import xiao.customgun.core.api.minecraft.capability.IInventoryCapability;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.gun._ReloadData;
import xiao.customgun.core.resource.data.data.gun.reload._ReloadCooldownData;
import xiao.customgun.core.resource.data.data.gun.reload._ReloadFeedData;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

import java.util.List;

@ApiStatus.Internal
public class _DefaultGunAction {

    /**
     * 限定{@link BoltType#MANUAL_ACTION}
     */
    protected static boolean startBolt(ShooterProperty shooterProperty,
                                      @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                      ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        if (iGun.hasBarrelAmmo(gunItem)) { // 枪管有子弹则不拉栓
            return false;
        }

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return false;

        // 检查 bolt 类型是否是 manual action
        BoltType boltType = gunIndexInstance.getGunData().getBoltType();
        if (boltType != BoltType.MANUAL_ACTION) return false;

        // 检查是否有子弹可拉栓
        boolean hasAmmo = iGun.useInventoryAmmo(gunItem)
                ? iGun.hasInventoryAmmo(livingShooter, gunItem) // 背包直读
                : iGun.getMagAmmoCount(gunItem) < 1;
        if (!hasAmmo) return false;

        return true;
    }

    /**
     * {@link _DefaultGunAction#startBolt}已经限定了{@link BoltType#MANUAL_ACTION}
     * @return 是否还在拉栓
     */
    protected static boolean tickBolt(ShooterProperty shooterProperty,
                                      @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                      ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        float rawBoltFeedTime = gunData.getBoltFeedTime();
        long boltActionTimeMs = (long) (gunData.getBoltActionTime() * 1000);
        long boltFeedTime = rawBoltFeedTime <= 0 ? boltActionTimeMs : (long) (rawBoltFeedTime * 1000);

        float boltTime = shooterProperty.isBolting ? System.currentTimeMillis() - shooterProperty.boltTimestamp : 0;
        if (boltTime < boltFeedTime) { // 没到上弹时间
            return true;
        }

        BoltType boltType = gunData.getBoltType();
        if (boltType == BoltType.OPEN_BOLT) return false; // open bolt不需要拉栓

        // 仅在枪管没子弹才上弹 (一次bolt只触发一次)
        if (!iGun.hasBarrelAmmo(gunItem)) {
            int consumedAmmo = iGun.useInventoryAmmo(gunItem)
                    ? iGun.consumeAmmoOnce(livingShooter, gunItem) // 背包直读
                    : iGun.consumeMagAmmo(gunItem); // 枪械弹匣供弹 (要先装弹)
            if (consumedAmmo > 0) {
                // 上弹到枪管
                iGun.setBarrelAmmoCount(gunItem, consumedAmmo);
            }
        }

        if (boltTime >= gunData.getBoltActionTime()) { // 完成bolt
            return false;
        }

        return true;
    }

    protected static boolean canReload(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                       ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        _ReloadData reloadData = gunData.getReloadData();
        if (
                // 免费供弹则不需要消耗实际子弹
                reloadData.getFreeAmmoFeed()
                // 不需要检查子弹
                || !iLivingShooter.cgc$needCheckAmmo()
        ) return true;

//        BoltType boltType = gunData.getBoltType();
//        int currentAmmoCount = iGun.getMagAmmoCountWithBarrel(gunItem, boltType);
        // ↓装弹只管弹匣，不管枪管↑
        int currentAmmoCount = iGun.getMagAmmoCount(gunItem);
        int magAmmoLimit = iGun.getMagAmmoLimit(gunItem);

        if (
                // 达到容量就不用装
                currentAmmoCount >= magAmmoLimit
                // 背包直读不需要换弹
                || iGun.useInventoryAmmo(gunItem)
                // 还有虚拟备弹
                || iGun.useDummyAmmo(gunItem) && iGun.getDummyAmmoCount(gunItem) > 0
        ) return false;

        // 检查背包内子弹库存
        IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingShooter, null);
        if (inventoryCapability == null) return false;
        for (int i = 0; i < inventoryCapability.getContainerSize(); i++) {
            final ItemStack ammoItem = inventoryCapability.getItemReadOnly(i);

            if (iGun.isMatchedAmmo(gunItem, ammoItem)) {
                return true;
            }
        }

        // 没提前返回就是没找到
        return false;
    }

    protected static boolean startReload(ShooterProperty shooterProperty,
                                         @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                         ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        return true;
    }

    protected static ReloadState tickReload(ShooterProperty shooterProperty,
                                            @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                            ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        ReloadState reloadState = new ReloadState();

        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) {
            reloadState.setStateType(ReloadState.StateType.NOT_RELOADING);
            return reloadState;
        }

        GunData gunData = gunIndexInstance.getGunData();
        _ReloadData reloadData = gunData.getReloadData();
        ReloadState.StateType stateType = ReloadState.StateType.NOT_RELOADING; // 编译器的静态检查得增加default分支
        ReloadState.StateType oldStateType = shooterProperty.reloadStateType;
        long countDown = ReloadState.NOT_RELOADING_COUNTDOWN; // 编译器的静态检查得增加default分支
        long progressMs = shooterProperty.reloadTimestamp > 0 ? System.currentTimeMillis() - shooterProperty.reloadTimestamp : 0;

        // 计算新的 stateType 和 countDown
        ReloadState.StateCategory stateCategory = oldStateType.getCategory();
        boolean finished = false;
        switch (stateCategory) {
            case EMPTY_RELOAD, TACTICAL_RELOAD -> {
                long feedTimeMs;
                long finishingTimeMs;
                _ReloadFeedData reloadFeedData = reloadData.getReloadFeed();
                _ReloadCooldownData reloadCooldownData = reloadData.getReloadCooldown();

                if (stateCategory == ReloadState.StateCategory.EMPTY_RELOAD) {
                    feedTimeMs = (long) (reloadFeedData.getEmpty() * 1000);
                    finishingTimeMs = (long) (reloadCooldownData.getEmpty() * 1000);
                } else {
                    feedTimeMs = (long) (reloadFeedData.getTactical() * 1000);
                    finishingTimeMs = (long) (reloadCooldownData.getTactical() * 1000);
                }

                if (progressMs < feedTimeMs) { // 还没上弹
                    stateType = stateCategory == ReloadState.StateCategory.EMPTY_RELOAD ? ReloadState.StateType.EMPTY_RELOAD_FEEDING : ReloadState.StateType.TACTICAL_RELOAD_FEEDING;
                    countDown = feedTimeMs - progressMs;
                } else if (progressMs < finishingTimeMs) { // 已上弹，在收尾
                    stateType = stateCategory == ReloadState.StateCategory.EMPTY_RELOAD ? ReloadState.StateType.EMPTY_RELOAD_FINISHING : ReloadState.StateType.TACTICAL_RELOAD_FINISHING;
                    countDown = finishingTimeMs - progressMs;
                } else { // 已结束
                    finished = true;
                    stateType = ReloadState.StateType.NOT_RELOADING;
                    countDown = ReloadState.NOT_RELOADING_COUNTDOWN;
                }
            }
            case NOT_RELOADING -> {
                stateType = ReloadState.StateType.NOT_RELOADING;
                countDown = ReloadState.NOT_RELOADING_COUNTDOWN;
            }
            // 增加类型使此处强制编译不通过
        }

        // (之前没装弹/在feeding) & (现在切换到finishing/已经finish)
        // 实际应该不会有，也不允许有 从 not reloading 到 finishing/finish，所以能从 stateType 获取到 isTactical
        if ((!oldStateType.isReloading() || oldStateType.isReloadFeeding())
                && (stateType.isReloadFinishing() || finished)) {
            _defaultReloadFeed(gunData, iGun, gunItem, iLivingShooter, livingShooter, stateType.isReloadingTactical());
        }

        reloadState.setStateType(stateType);
        reloadState.setCountDown(countDown);
        return reloadState;
    }
    private static void _defaultReloadFeed(GunData gunData,
                                           @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                           ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                           boolean isTactical) {
        int needAmmoCount = iGun.getMagAmmoLimit(gunItem) - iGun.getMagAmmoCount(gunItem);
        _ReloadData reloadData = gunData.getReloadData();
        boolean needConsumeAmmo = iLivingShooter.cgc$needCheckAmmo() || reloadData.getFreeAmmoFeed();
        int consumedAmmo = 0;
        AmmoFeedType ammoFeedType = reloadData.getAmmoFeedType();
        switch (ammoFeedType) {
            // TODO
            case MAGAZINE -> {
                if (needConsumeAmmo) consumedAmmo = consumeAmmoFromPlayer(iGun, gunItem, iLivingShooter, livingShooter, needAmmoCount);
                if (consumedAmmo > 0) {
                }
            }
            case MANUAL -> {
            }
            case FUEL -> {
                if (needConsumeAmmo) consumedAmmo = consumeAmmoFromPlayer(iGun, gunItem, iLivingShooter, livingShooter, needAmmoCount);
                if (consumedAmmo > 0) {
                }
            }
            case INVENTORY -> {
            }
            // 增加类型使此处强制编译不通过
        }
        // 如果不是战术换弹，需要将弹匣中的一枚子弹放到枪膛中
        // TODO
    }
    /**
     * 获取不到玩家 则 默认消耗成功
     */
    @ApiStatus.Internal
    public static int consumeAmmoFromPlayer(IGun iGun, ItemStack gunItem,
                                            @Nullable ILivingShooter iLivingShooter, @Nullable LivingEntity livingShooter,
                                            int neededAmount) {
        // 如果处于背包直读并且创造模式不消耗的情况
        if (iGun.useInventoryAmmo(gunItem) && !(iLivingShooter == null || iLivingShooter.cgc$needCheckAmmo())) return neededAmount;

        if (iGun.useDummyAmmo(gunItem)) return iGun.findAndExtractDummyAmmo(gunItem, neededAmount);
        else {
            if (livingShooter == null) return neededAmount;
            IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingShooter, null);
            return iGun.findAndExtractInventoryAmmo(inventoryCapability, gunItem, neededAmount);
        }
    }

    protected static void interruptReload(ShooterProperty shooterProperty,
                                          @NotNull IGun iGun, @NotNull ItemStack gunItem,
                                          ILivingShooter iLivingShooter, LivingEntity livingShooter) {
        return;
    }

    protected static boolean switchFireMode(ShooterProperty shooterProperty,
                                         @NotNull IGun iGun, @NotNull ItemStack gunItem) {
        var gunLocation = iGun.getGunLocation(gunItem);
        @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        List<FireModeType> fireModeTypes = gunData.getFireModeTypes();
        if (fireModeTypes.isEmpty()) return false;

        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        int nextIndex = fireModeTypes.indexOf(fireModeType) + 1;
        fireModeType = fireModeTypes.get(nextIndex % fireModeTypes.size());
        iGun.setFireModeType(gunItem, fireModeType);
        return true;
    }
}
