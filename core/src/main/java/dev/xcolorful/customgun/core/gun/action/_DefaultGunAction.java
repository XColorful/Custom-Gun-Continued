/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.gun.action;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ReloadState;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.AmmoFeedType;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.gun._ReloadData;
import dev.xcolorful.customgun.core.resource.data.data.gun.reload._ReloadCooldownData;
import dev.xcolorful.customgun.core.resource.data.data.gun.reload._ReloadFeedData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        // 检查是否使用枪管子弹
        BoltType boltType = gunIndexInstance.getGunData().getBoltType();
        if (!boltType.useBarrelAmmo()
//                || boltType.autoBoltBarrelAmmo() // 取消这个限制，使得全自动步枪也能在拿到手后上一下弹
        ) return false;

        // 检查是否有子弹可拉栓
        boolean hasAmmo = iGun.useInventoryAmmo(gunItem)
                ? iGun.hasInventoryAmmo(livingShooter, gunItem) // 背包直读
                : iGun.getMagAmmoCount(gunItem) > 0;
        if (!hasAmmo) return false;

        return true;
    }

    /**
     * {@link _DefaultGunAction#startBolt}已经限定了{@link BoltType#useBarrelAmmo()}
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

        // 仅在枪管没子弹才上弹 (一次bolt只触发一次，但防不住换弹期间意外消耗掉枪管子弹)
        if (!iGun.hasBarrelAmmo(gunItem)) {
            int boltAmmoCount =  iGun.boltBarrelAmmo(livingShooter, gunItem);
            if (boltAmmoCount <= 0) return false; // 拉栓上膛失败/无效
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
        int currentMagAmmoCount = iGun.getMagAmmoCount(gunItem);
        int magAmmoLimit = iGun.getMagAmmoLimit(gunItem);

        if (
                // 达到容量就不用装
                currentMagAmmoCount >= magAmmoLimit
                // 背包直读不需要换弹
                || iGun.useInventoryAmmo(gunItem)
                // 还有虚拟备弹
                || iGun.useDummyAmmo(gunItem) && iGun.getDummyAmmoCount(gunItem) > 0
        ) return false;

        // 检查背包内子弹库存
        @Nullable IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingShooter, null);
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
            // 增加类型需检查
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
        int magAmmoCount = iGun.getMagAmmoCount(gunItem);
        int needAmmoCount = iGun.getMagAmmoLimit(gunItem) - magAmmoCount;
        _ReloadData reloadData = gunData.getReloadData();
        boolean needConsumeAmmo = iLivingShooter.cgc$needCheckAmmo() || reloadData.getFreeAmmoFeed();
        int consumedAmmo;
        AmmoFeedType ammoFeedType = reloadData.getAmmoFeedType();
        switch (ammoFeedType) {
            case MAGAZINE, MANUAL -> {
                // 手动供弹只能装一发
                if (ammoFeedType == AmmoFeedType.MANUAL) needAmmoCount = Math.min(needAmmoCount, 1);

                consumedAmmo = needConsumeAmmo ? consumeAmmoFromPlayer(iGun, gunItem, iLivingShooter, livingShooter, needAmmoCount)
                        : needAmmoCount;
                if (consumedAmmo > 0) {
                    iGun.setMagAmmoCount(gunItem, magAmmoCount + consumedAmmo);
                }
            }
            case FUEL -> {
                // 消耗单个燃料物品补满弹药
                consumedAmmo = needConsumeAmmo ? consumeAmmoFromPlayer(iGun, gunItem, iLivingShooter, livingShooter, 1)
                        : needAmmoCount;
                if (consumedAmmo > 0) {
                    iGun.setMagAmmoCount(gunItem, magAmmoCount + needAmmoCount);
                }
            }
            case INVENTORY -> {
                // 背包直读不需要把子弹装到枪上
            }
            // 增加类型需检查
        }

        // 如果不是战术换弹，需要执行上膛
        if (!isTactical) {
            iGun.boltBarrelAmmo(livingShooter, gunItem);
        }
    }
    /**
     * 获取不到玩家 则 只能消耗枪械上的子弹
     */
    @ApiStatus.Internal
    public static int consumeAmmoFromPlayer(IGun iGun, ItemStack gunItem,
                                            @Nullable ILivingShooter iLivingShooter, @Nullable LivingEntity livingShooter,
                                            int neededAmount) {
        if (neededAmount <= 0) return 0;

        // 如果处于背包直读并且创造模式不消耗的情况
        if (iGun.useInventoryAmmo(gunItem) && !(iLivingShooter == null || iLivingShooter.cgc$needCheckAmmo())) return neededAmount;

        if (iGun.useDummyAmmo(gunItem)) return iGun.findAndExtractDummyAmmo(iGun, gunItem, neededAmount);
        else {
            if (livingShooter == null) return 0;
            @Nullable IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingShooter, null);
            if (inventoryCapability == null) return 0;
            return iGun.findAndExtractInventoryAmmo(inventoryCapability, iGun, gunItem, neededAmount);
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
