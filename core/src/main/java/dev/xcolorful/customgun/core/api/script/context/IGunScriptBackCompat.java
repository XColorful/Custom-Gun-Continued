/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.script.context;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.event.CycledEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.MagazineCategory;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.IAttachmentModifier;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.minecraft.capability.IInventoryCapability;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.api.resource.ResourceTag;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterAspect;
import dev.xcolorful.customgun.core.entity.shooter.LivingShooterShoot;
import dev.xcolorful.customgun.core.gun.action._DefaultGunAction;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.gun._BurstData;
import dev.xcolorful.customgun.core.resource.data.data.gun._ChargingData;
import dev.xcolorful.customgun.core.resource.data.data.gun._HeatData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

import java.util.Map;

/**
 * 用来兼容旧lua脚本API的public方法
 */
public interface IGunScriptBackCompat extends IGunScriptContextAccess {

    /**
     * TODO 该方法需要AttachmentModifier添加所有deprecated的属性
     */
    default @Nullable LuaValue getCachedProperty(String id) {
        AttachmentModifierType modifierType = AttachmentModifierType.fromString(id);
        if (modifierType == null) return LuaValue.NIL;

        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterGunModifierCache cache = iLivingShooter != null ? iLivingShooter.cgc$getGunModifierCache() : null;
        if (cache == null) return LuaValue.NIL;
        else return cache.getValue(modifierType, IAttachmentModifier.class); // runtime+只有string，没法编译期检查
    }

    /**
     * 处理一次射击的过热变化
     */
    default void handleShootHeat() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return;
        @Nullable _HeatData heatData = gunIndexInstance.getGunData().getHeatData();
        if (heatData == null) return;

        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        float newHeat = Math.min(iGun.getHeatCount(gunItem) + heatData.getHeatPerShot(), heatData.getMaxHeat());
        iGun.setHeatCount(gunItem, newHeat);
        if (newHeat >= heatData.getMaxHeat()) {
            iGun.setOverheatLock(gunItem, true);
        }
    }

    /**
     * @return 是否成功消耗子弹
     */
    default boolean reduceAmmoOnce() {
        IGun iGun = this.getIGun();
        @Nullable LivingEntity livingShooter = this.getLivingShooter();
        ItemStack gunItem = this.getGunItem();
        return iGun.consumeAmmoOnce(livingShooter, gunItem) > 0;
    }

    /**
     * @return 开始换弹到现在的时间，单位ms
     */
    default long getReloadTime() {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return 0;
        else return System.currentTimeMillis() - shooterProperty.reloadTimestamp;
    }

    /**
     * @return 开始拉栓到现在的时间，单位ms
     */
    default long getBoltTime() {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null || !shooterProperty.isBolting) return 0;
        else return System.currentTimeMillis() - shooterProperty.boltTimestamp;
    }

    /**
     * @return 枪械的射击间隔，单位毫秒
     */
    default long getShootInterval() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        FireModeType fireModeType = iGun.getFireModeType(gunItem);
        long cooldown;
        if (fireModeType == FireModeType.BURST) {
            _BurstData burstData = gunData.getBurstData();
            cooldown = (long) (burstData.getShootIntervalSeconds() * 1000f);
        } else {
            @Nullable LivingEntity livingShooter = this.getLivingShooter();
            if (livingShooter == null) return 0;
            cooldown = LivingShooterShoot._getShootInterval(livingShooter, gunData, fireModeType, iGun, gunItem);
        }
        // 给 5ms 的窗口时间，以平衡延迟
        cooldown -= LivingShooterAspect.WINDOW_TIME_MS;
        return Math.max(cooldown, 0);
    }

    /**
     * @return 上次射击的 timestamp ms，在切枪时会重置为 -1
     */
    default long getLastShootTimestamp() {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return 0;
        return shooterProperty.lastShootTimestamp - shooterProperty.baseTimestamp;
    }

    /**
     * 调整射击间隔。
     * 射击间隔比较特殊，它在客户端和服务端上是分别计算的。因此你还需要在状态机脚本中重复进行一次这个操作。
     *
     * @param alpha 需要加上或减少的射击间隔，单位为毫秒。正数即增加射击间隔，负数则是减少。
     */
    default void adjustShootInterval(long alpha) {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return;
        shooterProperty.shootTimestamp += alpha;
    }

    /**
     * @param alpha 需要加上或减少的换弹时间，单位为毫秒。正数即增加换弹时间（加快换弹进度），负数则是减少（减慢换弹进度）。
     */
    default void adjustReloadTime(long alpha) {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return;
        shooterProperty.shootTimestamp -= alpha;
    }

    /**
     * @param alpha 需要加上或减少的拉栓时间，单位为毫秒。正数即增加拉栓时间（加快拉栓进度），负数则是减少（减慢拉栓进度）。
     */
    default void adjustBoltTime(long alpha) {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return;
        shooterProperty.boltTimestamp -= alpha;
    }

    /**
     * @return 范围 0~1。0 代表未瞄准，1 代表瞄准完成。
     */
    default float getAimingProgress() {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return 0;
        return shooterProperty.aimingProgress;
    }

    /**
     * @return 本次射击的蓄力进度。此上下文仅在射击流程期间可用，非射击期间调用时返回的值没有意义
     */
    default float getChargeProgress() {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return 0;
        return shooterProperty.chargeProgress;
    }

    /**
     * 当前开火模式下，枪械是否有蓄力配置
     */
    default boolean hasChargeData() {
        return getChargeData(this) != null;
    }
    /**
     * 当前开火模式下，蓄力的最大进度
     */
    default float getMaxCharge() {
        _ChargingData chargingData = getChargeData(this);
        return chargingData != null ? chargingData.getMaxCharge() : 0f;
    }
    /**
     * 当前开火模式下，蓄力的开火阈值
     */
    default float getFireThreshold() {
        _ChargingData chargingData = getChargeData(this);
        return chargingData != null ? chargingData.getFireThreshold() : 0f;
    }
    static @Nullable _ChargingData getChargeData(IGunScriptBackCompat _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.getGunIndexInstance();
        if (gunIndexInstance == null) return null;

        GunData gunData = gunIndexInstance.getGunData();
        Map<FireModeType, _ChargingData> chargingData = gunData.getChargingData();
        IGun iGun = _this.getIGun();
        ItemStack gunItem = _this.getGunItem();
        return chargingData.get(iGun.getFireModeType(gunItem));
    }

    /**
     * 计算本次射击的蓄力进度。此上下文仅在射击流程期间可用，非射击期间调用时返回的值没有意义
     */
    default float getChargeRatio() {
        float maxCharge = this.getMaxCharge();
        if (maxCharge <= 0f) return 0f;
        else return Math.max(0f, Math.min(this.getChargeProgress() / maxCharge, 1f));
    }

    /**
     * @return 玩家当前的换弹状态 (序数)
     */
    default int getReloadStateType() {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return 0;
        return shooterProperty.reloadStateType.getIndex();
    }

    /**
     * @return 枪械当前的开火模式 (序数)
     */
    default int getFireMode() {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        return iGun.getFireModeType(gunItem).getIndex();
    }

    /**
     * @return 射击是否需要消耗弹药。经过设置，创造模式的玩家可以不消耗弹药射击。
     */
    default boolean isShootingNeedConsumeAmmo() {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        return iLivingShooter == null || iLivingShooter.cgc$consumesAmmoOrNot();
    }

    /**
     * @return 换弹是否需要消耗弹药。一般来说创造模式下不需要消耗弹药。
     */
    default boolean isReloadingNeedConsumeAmmo() {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        return iLivingShooter == null || iLivingShooter.cgc$needCheckAmmo();
    }

    /**
     * {@link #getMaxAmmoCount} - {@link IGun#getMagAmmoCount}
     * @return 当前枪械需要的弹药数量
     */
    default int getNeededAmmoAmount() {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        int magAmmoLimit = iGun.getMagAmmoLimit(gunItem);
        int currentAmmoCount = this.getAmmoCountInMagazine();
        return magAmmoLimit - currentAmmoCount;
    }

    /**
     * @return 返回弹匣中的备弹数，不计算已在枪管中的弹药。
     */
    @Deprecated
    default int getAmmoAmount() {
        return this.getAmmoCountInMagazine();
    }

    /**
     * @return 返回枪械弹匣的最大备弹数，不计算已在枪管中的弹药。
     */
    default int getMaxAmmoCount() {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        return iGun.getMagAmmoLimit(gunItem);
    }

    /**
     * @return 扩容等级，范围 0 ~ 3。0 表示没有安装扩容弹匣，1 ~ 3 表示安装了扩容等级 1 ~ 3 的扩容弹匣
     */
    default int getMagExtentLevel() {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        var attachmentLocation = iGun.getAttachmentLocation(gunItem, AttachmentCategory.MAGAZINE);
        AttachmentData attachmentData = ResourceApi.getAttachmentData(attachmentLocation);
        if (attachmentData == null) return 0;
        @Nullable MagazineCategory magazineCategory = attachmentData.getMagazineCategory();
        return magazineCategory != null ? magazineCategory.getIndex() : 0;
    }

    /**
     * 尽可能多地从玩家身上 (或者虚拟备弹) 消耗掉弹药，返回消耗的数量
     * @param neededAmount 需要的弹药数量
     * @return 实际消耗的弹药数量
     */
    default int consumeAmmoFromPlayer(int neededAmount) {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        @Nullable LivingEntity livingShooter = this.getLivingShooter();
        return _DefaultGunAction.consumeAmmoFromPlayer(iGun, gunItem, iLivingShooter, livingShooter, neededAmount);
    }

    /**
     * 检查玩家身上（或者虚拟备弹）是否有弹药可以消耗，通常用于循环换弹的打断。
     * 创造模式的玩家会直接返回 true
     * @return 玩家身上（或者虚拟备弹）是否有弹药可以消耗
     */
    default boolean hasAmmoToConsume() {
        if (!this.isReloadingNeedConsumeAmmo()) return true;
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        if (iGun.useDummyAmmo(gunItem)) return iGun.getDummyAmmoCount(gunItem) > 0;
        else {
            @Nullable LivingEntity livingShooter = this.getLivingShooter();
            if (livingShooter == null) return false;
            @Nullable IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(livingShooter, null);
            // TODO 仅查询子弹的IGun runtime api
            return false;
        }
    }

    /**
     * 将子弹推入弹匣
     * @param amount 需要推入的子弹数量
     * @return 多余的子弹
     */
    default int putAmmoInMagazine(int amount) {
        if (amount < 0) return 0;
        int maxAmmoCount = this.getMaxAmmoCount();
        int currentAmmoCount = this.getAmmoCountInMagazine();
        int newAmmoCount = currentAmmoCount + amount;
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        if (maxAmmoCount < newAmmoCount) {
            iGun.setMagAmmoCount(gunItem, maxAmmoCount);
            return newAmmoCount - maxAmmoCount;
        } else {
            iGun.setMagAmmoCount(gunItem, newAmmoCount);
            return 0;
        }
    }

    /**
     * 将子弹从弹匣移除
     * @param amount 需要移除的数量
     * @return 成功移除的数量
     */
    default int removeAmmoFromMagazine(int amount) {
        if (amount < 0) return 0;
        int currentAmmoCount = this.getAmmoCountInMagazine();
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        if (currentAmmoCount < amount) {
            iGun.setMagAmmoCount(gunItem, 0);
            return currentAmmoCount;
        } else {
            iGun.setMagAmmoCount(gunItem, currentAmmoCount - amount);
            return amount;
        }
    }

    /**
     * @return 弹匣内子弹数量
     */
    default int getAmmoCountInMagazine() {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        return iGun.getMagAmmoCount(gunItem);
    }

    /**
     * 获取枪膛内是否有子弹
     * @return 枪膛内是否有子弹.如果是开膛待击的枪械，则此方法返回 false
     */
    default boolean hasAmmoInBarrel() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        BoltType boltType = gunData.getBoltType();
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        return boltType.useBarrelAmmo() && iGun.hasBarrelAmmo(gunItem);
    }

    default void setAmmoInBarrel(int ammoInBarrel) {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        iGun.setBarrelAmmoCount(gunItem, ammoInBarrel);
    }
    @Deprecated(forRemoval = false)
    default void setAmmoInBarrel(boolean ammoInBarrel) {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        iGun.setBulletInBarrel(gunItem, ammoInBarrel);
    }

    /**
     * 将任意 lua 对象数据缓存到玩家数据中。用于脚本中异步传递数据，或者跨方法传递数据
     * @param luaValue 缓存的 lua 对象
     */
    default void cacheScriptData(LuaValue luaValue) {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return;
        shooterProperty.scriptData = luaValue;
    }

    /**
     * 将玩家数据中缓存的 lua 对象取出
     * @return 缓存的 lua 对象
     */
    default @Nullable LuaValue getCachedScriptData() {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return LuaValue.NIL;
        return shooterProperty.scriptData;
    }

    /**
     * 委托延迟的循环任务，在主线程执行，是线程安全的，但是时间不是严格的，粒度取决于 TPS
     * @param value    应当是一个返回 boolean 的 LuaFunction。如果返回 false ，则将退出循环。
     * @param delayMs  延迟执行的时间。
     * @param periodMs 循环执行的间隔。
     * @param cycles   最大循环次数。-1 代表无限次。
     */
    default void safeAsyncTask(LuaValue value, long delayMs, long periodMs, int cycles) {
        if (!value.isfunction()) return;
        LuaFunction func = value.checkfunction();
        CycledEvent.create(() -> func.call().checkboolean(), delayMs, periodMs, cycles < 0 ? Integer.MAX_VALUE : cycles);
    }

    /**
     * 获取当前系统时间，单位毫秒
     * @return 当前系统时间
     */
    default long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    @Deprecated String EMPTY_ATTACHMENT_ID = "tacz:empty";
    /**
     * 获取枪械的配件 ID
     * @return 配件 ID, 如果类型错误或者对应的配件不存在则返回空配件 ID 'tacz:empty'
     */
    @Deprecated(forRemoval = false)
    default String getAttachment(String type) {
        AttachmentCategory attachmentCategory = AttachmentCategory.fromString(type);
        if (attachmentCategory == null) return EMPTY_ATTACHMENT_ID;

        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        @NotNull var attachmentLocation = iGun.getAttachmentLocation(gunItem, attachmentCategory);
        if (ResourceTag.NULL_LOCATION.equals(attachmentLocation)) return EMPTY_ATTACHMENT_ID;
        else return attachmentCategory.toString();
    }

    default @Nullable LivingEntity getShooter() {
        return this.getLivingShooter();
    }

    default ItemStack getItemStack() {
        return this.getGunItem();
    }

    @Deprecated
    default _AbstractGunItem getAbstractGunItem() {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        return new _AbstractGunItem(iGun, gunItem);
    }

    default void setHeatAmount(float amount) {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        iGun.setHeatCount(gunItem, amount);
    }

    default float getHeatAmount() {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        return iGun.getHeatCount(gunItem);
    }

    default boolean hasHeatData() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        return gunData.getHeatData() != null;
    }

    default float getHeatMinRpm() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getMinRpmByHeat() : 0;
    }

    default float getHeatMaxRpm() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getMaxRpmByHeat() : 0;
    }

    default float getHeatMinInaccuracy() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getMinInaccuracyByHeat() : 0;
    }

    default float getHeatMaxInaccuracy() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getMaxInaccuracyByHeat() : 0;
    }

    default float getHeatMax() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getMaxHeat() : 0;
    }

    default float getHeatPerShot() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getHeatPerShot() : 0;
    }

    default boolean isOverheatLocked() {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        return iGun.hasOverheatLock(gunItem);
    }

    default void setOverheatLocked(boolean locked) {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        iGun.setOverheatLock(gunItem, locked);
    }

    default long getOverheatTime() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getOverheatLocktimeMs() : 0;
    }

    default long getCoolingDelay() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getCoolingDelayMs() : 0;
    }

    default float calcHeatReduction(long heatTimestamp) {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        if (heatData == null) return 0;
        else return ((float) (System.currentTimeMillis() - heatTimestamp) / 10_000f) * heatData.getCoolingSpeedMultiplier();
    }

    default int getBoltByInt() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        BoltType boltType = gunData.getBoltType();
        return boltType.getIndex();
    }

    default BoltType getBolt() {
        @Nullable GunIndexInstance gunIndexInstance = this.getGunIndexInstance();
        if (gunIndexInstance == null) return null;

        GunData gunData = gunIndexInstance.getGunData();
        return gunData.getBoltType();
    }

    default void setDataHolder(ShooterProperty dataHolder) {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        if (iLivingShooter == null) return;
        throw new IllegalStateException("_GunScriptBackCompat: Reject to set data holder, DO NOT USE setDataHolder");
    }

    default boolean useInventoryAmmo() {
        IGun iGun = this.getIGun();
        ItemStack gunItem = this.getGunItem();
        return iGun.useInventoryAmmo(gunItem);
    }

    default ShooterProperty getDataHolder() {
        @Nullable ILivingShooter iLivingShooter = this.getILivingShooter();
        if (iLivingShooter == null) return null;
        return iLivingShooter.cgc$getShooterProperty();
    }
}
