/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.gun.script;

import dev.xcolorful.customgun.CustomGun;
import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.entity.ShooterProperty;
import dev.xcolorful.customgun.core.api.entity.shooter.ILivingShooterGetter;
import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.event.CycledEvent;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.MagazineCategory;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.AttachmentModifierType;
import dev.xcolorful.customgun.core.api.item.attachment.modifier.IAttachmentModifier;
import dev.xcolorful.customgun.core.api.item.gun.BoltType;
import dev.xcolorful.customgun.core.api.item.gun.FireModeType;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 用来兼容旧lua脚本API的public方法
 */
@Deprecated(forRemoval = false)
public class _GunScriptBackCompat {

    /**
     * TODO 该方法需要AttachmentModifier添加所有deprecated的属性
     */
    protected static LuaValue getCachedProperty(GunScriptApi _this, String id) {
        AttachmentModifierType modifierType = AttachmentModifierType.fromString(id);
        if (modifierType == null) return LuaValue.NIL;

        ShooterGunModifierCache cache = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getGunModifierCache() : null;
        if (cache == null) return LuaValue.NIL;
        else return cache.getValue(modifierType, IAttachmentModifier.class); // runtime+只有string，没法编译期检查
    }

    /**
     * 执行一次完整的射击逻辑，会考虑玩家的状态(是否在瞄准、是否在移动、是否在匍匐等)、配件数值影响、多弹丸散射、连发，播放开火音效、
     * @param consumeAmmo 本次射击是否消耗弹药
     */
    protected static void shootOnce(GunScriptApi _this, boolean consumeAmmo) {
        @Nullable ILivingShooter iLivingShooter = _this.iLivingShooter;
        @Nullable ShooterProperty shooterProperty = iLivingShooter != null ? iLivingShooter.cgc$getShooterProperty() : null;
        _this.iGun.gunFire(shooterProperty,
                _this.iGun, _this.gunItem,
                iLivingShooter, _this.livingShooter,
                _this.pitchSupplier, _this.yawSupplier);
    }

    /**
     * 处理一次射击的过热变化
     */
    protected static void handleShootHeat(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return;
        @Nullable _HeatData heatData = gunIndexInstance.getGunData().getHeatData();
        if (heatData == null) return;

        float newHeat = Math.min(_this.iGun.getHeatCount(_this.gunItem) + heatData.getHeatPerShot(), heatData.getMaxHeat());
        _this.iGun.setHeatCount(_this.gunItem, newHeat);
        if (newHeat >= heatData.getMaxHeat()) {
            _this.iGun.setOverheatLock(_this.gunItem, true);
        }
    }

    /**
     * @return 是否成功消耗子弹
     */
    protected static boolean reduceAmmoOnce(GunScriptApi _this) {
        return _this.iGun.consumeAmmoOnce(_this.livingShooter, _this.gunItem) > 0;
    }

    /**
     * @return 开始换弹到现在的时间，单位ms
     */
    protected static long getReloadTime(GunScriptApi _this) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return 0;
        else return System.currentTimeMillis() - shooterProperty.reloadTimestamp;
    }

    /**
     * @return 开始拉栓到现在的时间，单位ms
     */
    protected static long getBoltTime(GunScriptApi _this) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null || !shooterProperty.isBolting) return 0;
        else return System.currentTimeMillis() - shooterProperty.boltTimestamp;
    }

    /**
     * @return 枪械的射击间隔，单位毫秒
     */
    protected static long getShootInterval(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        FireModeType fireModeType = _this.iGun.getFireModeType(_this.gunItem);
        long cooldown;
        if (fireModeType == FireModeType.BURST) {
            _BurstData burstData = gunData.getBurstData();
            cooldown = (long) (burstData.getShootIntervalSeconds() * 1000f);
        } else {
            if (_this.livingShooter == null) return 0;
            cooldown = LivingShooterShoot._getShootInterval(_this.livingShooter, gunData, fireModeType, _this.iGun, _this.gunItem);
        }
        // 给 5ms 的窗口时间，以平衡延迟
        cooldown -= LivingShooterAspect.WINDOW_TIME_MS;
        return Math.max(cooldown, 0);
    }

    /**
     * @return 上次射击的 timestamp ms，在切枪时会重置为 -1
     */
    protected static long getLastShootTimestamp(GunScriptApi _this) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return 0;
        return shooterProperty.lastShootTimestamp - shooterProperty.baseTimestamp;
    }

    /**
     * 调整射击间隔。
     * 射击间隔比较特殊，它在客户端和服务端上是分别计算的。因此你还需要在状态机脚本中重复进行一次这个操作。
     *
     * @param alpha 需要加上或减少的射击间隔，单位为毫秒。正数即增加射击间隔，负数则是减少。
     */
    protected static void adjustShootInterval(GunScriptApi _this, long alpha) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return;
        shooterProperty.shootTimestamp += alpha;
    }

    /**
     * @param alpha 需要加上或减少的换弹时间，单位为毫秒。正数即增加换弹时间（加快换弹进度），负数则是减少（减慢换弹进度）。
     */
    protected static void adjustReloadTime(GunScriptApi _this, long alpha) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return;
        shooterProperty.shootTimestamp -= alpha;
    }

    /**
     * @param alpha 需要加上或减少的拉栓时间，单位为毫秒。正数即增加拉栓时间（加快拉栓进度），负数则是减少（减慢拉栓进度）。
     */
    protected static void adjustBoltTime(GunScriptApi _this, long alpha) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return;
        shooterProperty.boltTimestamp -= alpha;
    }

    /**
     * @return 范围 0~1。0 代表未瞄准，1 代表瞄准完成。
     */
    protected static float getAimingProgress(GunScriptApi _this) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return 0;
        return shooterProperty.aimingProgress;
    }

    /**
     * @return 本次射击的蓄力进度。此上下文仅在射击流程期间可用，非射击期间调用时返回的值没有意义
     */
    protected static float getChargeProgress(GunScriptApi _this) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return 0;
        return shooterProperty.chargeProgress;
    }

    /**
     * 当前开火模式下，枪械是否有蓄力配置
     */
    protected static boolean hasChargeData(GunScriptApi _this) {
        return getChargeData(_this) != null;
    }

    /**
     * 当前开火模式下，蓄力的最大进度
     */
    protected static float getMaxCharge(GunScriptApi _this) {
        _ChargingData chargingData = getChargeData(_this);
        return chargingData != null ? chargingData.getMaxCharge() : 0f;
    }

    /**
     * 当前开火模式下，蓄力的开火阈值
     */
    protected static float getFireThreshold(GunScriptApi _this) {
        _ChargingData chargingData = getChargeData(_this);
        return chargingData != null ? chargingData.getFireThreshold() : 0f;
    }

    /**
     * 计算本次射击的蓄力进度。此上下文仅在射击流程期间可用，非射击期间调用时返回的值没有意义
     */
    protected static float getChargeRatio(GunScriptApi _this) {
        float maxCharge = getMaxCharge(_this);
        if (maxCharge <= 0f) return 0f;
        else return Math.max(0f, Math.min(getChargeProgress(_this) / maxCharge, 1f));
    }

    /**
     * 设置本次射击的额外伤害倍率（0-256）。此方法仅在射击流程期间可用，非射击调用时没有任何意义
     */
    protected static void setShotDamageMultiplier(GunScriptApi _this, float multiplier) {
        _this.shotDamageMultiplier =  Mth.clamp(multiplier, 0f, 256f);
    }

    /**
     * 获取本次射击的额外伤害倍率。此方法仅在射击流程期间可用，非射击调用时没有任何意义
     */
    protected static float getShotDamageMultiplier(GunScriptApi _this) {
        return _this.shotDamageMultiplier;
    }

    /**
     * 设置本次射击的额外弹速倍率（0-256）。此方法仅在射击流程期间可用，非射击调用时没有任何意义
     */
    protected static void setProjectileSpeedMultiplier(GunScriptApi _this, float multiplier) {
        _this.projectileSpeedMultiplier = Mth.clamp(multiplier, 0f, 256f);
    }

    /**
     * 获取本次射击的额外弹速倍率。此方法仅在射击流程期间可用，非射击调用时没有任何意义
     */
    protected static float getProjectileSpeedMultiplier(GunScriptApi _this) {
        return _this.projectileSpeedMultiplier;
    }

    @Deprecated
    private static float clampMultiplier(float multiplier) {
        return Mth.clamp(multiplier, 0f, 256f);
    }

    /**
     * @return 玩家当前的换弹状态 (序数)
     */
    protected static int getReloadStateType(GunScriptApi _this) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return 0;
        return shooterProperty.reloadStateType.getIndex();
    }

    /**
     * @return 枪械当前的开火模式 (序数)
     */
    protected static int getFireMode(GunScriptApi _this) {
        return _this.iGun.getFireModeType(_this.gunItem).getIndex();
    }

    /**
     * @return 射击是否需要消耗弹药。经过设置，创造模式的玩家可以不消耗弹药射击。
     */
    protected static boolean isShootingNeedConsumeAmmo(GunScriptApi _this) {
        return _this.iLivingShooter == null || _this.iLivingShooter.cgc$consumesAmmoOrNot();
    }

    /**
     * @return 换弹是否需要消耗弹药。一般来说创造模式下不需要消耗弹药。
     */
    protected static boolean isReloadingNeedConsumeAmmo(GunScriptApi _this) {
        return _this.iLivingShooter == null || _this.iLivingShooter.cgc$needCheckAmmo();
    }

    /**
     * {@link #getMaxAmmoCount} - {@link IGun#getMagAmmoCount}
     * @return 当前枪械需要的弹药数量
     */
    protected static int getNeededAmmoAmount(GunScriptApi _this) {
        int magAmmoLimit = _this.iGun.getMagAmmoLimit(_this.gunItem);
        int currentAmmoCount = getAmmoCountInMagazine(_this);
        return magAmmoLimit - currentAmmoCount;
    }

    /**
     * @return 返回弹匣中的备弹数，不计算已在枪管中的弹药。
     */
    @Deprecated
    protected static int getAmmoAmount(GunScriptApi _this) {
        return getAmmoCountInMagazine(_this);
    }

    /**
     * @return 返回枪械弹匣的最大备弹数，不计算已在枪管中的弹药。
     */
    protected static int getMaxAmmoCount(GunScriptApi _this) {
        return _this.iGun.getMagAmmoLimit(_this.gunItem);
    }

    /**
     * @return 扩容等级，范围 0 ~ 3。0 表示没有安装扩容弹匣，1 ~ 3 表示安装了扩容等级 1 ~ 3 的扩容弹匣
     */
    protected static int getMagExtentLevel(GunScriptApi _this) {
        var attachmentLocation = _this.iGun.getAttachmentLocation(_this.gunItem, AttachmentCategory.MAGAZINE);
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
    protected static int consumeAmmoFromPlayer(GunScriptApi _this, int neededAmount) {
        return _DefaultGunAction.consumeAmmoFromPlayer(_this.iGun, _this.gunItem, _this.iLivingShooter, _this.livingShooter, neededAmount);
    }

    /**
     * 检查玩家身上（或者虚拟备弹）是否有弹药可以消耗，通常用于循环换弹的打断。
     * 创造模式的玩家会直接返回 true
     * @return 玩家身上（或者虚拟备弹）是否有弹药可以消耗
     */
    protected static boolean hasAmmoToConsume(GunScriptApi _this) {
        if (!isReloadingNeedConsumeAmmo(_this)) return true;
        else if (_this.iGun.useDummyAmmo(_this.gunItem)) return _this.iGun.getDummyAmmoCount(_this.gunItem) > 0;
        else {
            if (_this.livingShooter == null) return false;
            @Nullable IInventoryCapability inventoryCapability = CustomGun.getCapabilityProvider().getItemHandler(_this.livingShooter, null);
            return false;
        }
    }

    /**
     * 将子弹推入弹匣
     * @param amount 需要推入的子弹数量
     * @return 多余的子弹
     */
    protected static int putAmmoInMagazine(GunScriptApi _this, int amount) {
        if (amount < 0) return 0;
        int maxAmmoCount = getMaxAmmoCount(_this);
        int currentAmmoCount = getAmmoCountInMagazine(_this);
        int newAmmoCount = currentAmmoCount + amount;
        if (maxAmmoCount < newAmmoCount) {
            _this.iGun.setMagAmmoCount(_this.gunItem, maxAmmoCount);
            return newAmmoCount - maxAmmoCount;
        } else {
            _this.iGun.setMagAmmoCount(_this.gunItem, newAmmoCount);
            return 0;
        }
    }

    /**
     * 将子弹从弹匣移除
     * @param amount 需要移除的数量
     * @return 成功移除的数量
     */
    protected static int removeAmmoFromMagazine(GunScriptApi _this, int amount) {
        if (amount < 0) return 0;
        int currentAmmoCount = getAmmoCountInMagazine(_this);
        if (currentAmmoCount < amount) {
            _this.iGun.setMagAmmoCount(_this.gunItem, 0);
            return currentAmmoCount;
        } else {
            _this.iGun.setMagAmmoCount(_this.gunItem, currentAmmoCount - amount);
            return amount;
        }
    }

    /**
     * @return 弹匣内子弹数量
     */
    protected static int getAmmoCountInMagazine(GunScriptApi _this) {
        return _this.iGun.getMagAmmoCount(_this.gunItem);
    }

    /**
     * 获取枪膛内是否有子弹
     * @return 枪膛内是否有子弹.如果是开膛待击的枪械，则此方法返回 false
     */
    protected static boolean hasAmmoInBarrel(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        BoltType boltType = gunData.getBoltType();
        return boltType != BoltType.OPEN_BOLT && _this.iGun.hasBarrelAmmo(_this.gunItem);
    }

    protected static void setAmmoInBarrel(GunScriptApi _this, boolean ammoInBarrel) {
        _this.iGun.setBulletInBarrel(_this.gunItem, ammoInBarrel);
    }

    /**
     * 将任意 lua 对象数据缓存到玩家数据中。用于脚本中异步传递数据，或者跨方法传递数据
     * @param luaValue 缓存的 lua 对象
     */
    protected static void cacheScriptData(GunScriptApi _this, LuaValue luaValue) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return;
        shooterProperty.scriptData = luaValue;
    }

    /**
     * 将玩家数据中缓存的 lua 对象取出
     * @return 缓存的 lua 对象
     */
    protected static LuaValue getCachedScriptData(GunScriptApi _this) {
        ShooterProperty shooterProperty = _this.iLivingShooter != null ? _this.iLivingShooter.cgc$getShooterProperty() : null;
        if (shooterProperty == null) return LuaValue.NIL;
        return shooterProperty.scriptData;
    }

    /**
     * 获取在枪械 data 中声明的脚本参数
     * @return 脚本参数表
     */
    protected static LuaTable getScriptParam(GunScriptApi _this) {
        LuaTable param = _this.scriptParamsCache;
        return param == null ? new LuaTable() : param;
    }

    /**
     * 委托延迟的循环任务，在主线程执行，是线程安全的，但是时间不是严格的，粒度取决于 TPS
     * @param value    应当是一个返回 boolean 的 LuaFunction。如果返回 false ，则将退出循环。
     * @param delayMs  延迟执行的时间。
     * @param periodMs 循环执行的间隔。
     * @param cycles   最大循环次数。-1 代表无限次。
     */
    protected static void safeAsyncTask(GunScriptApi _this, LuaValue value, long delayMs, long periodMs, int cycles) {
        if (!value.isfunction()) return;
        LuaFunction func = value.checkfunction();
        CycledEvent.create(() -> func.call().checkboolean(), delayMs, periodMs, cycles <= 0 ? Integer.MAX_VALUE : cycles);
    }

    /**
     * 获取当前系统时间，单位毫秒
     * @return 当前系统时间
     */
    protected static long getCurrentTimestamp(GunScriptApi _this) {
        return System.currentTimeMillis();
    }

    @Deprecated public static final String EMPTY_ATTACHMENT_ID = "tacz:empty";
    /**
     * 获取枪械的配件 ID
     * @return 配件 ID, 如果类型错误或者对应的配件不存在则返回空配件 ID 'tacz:empty'
     */
    protected static String getAttachment(GunScriptApi _this, String type) {
        AttachmentCategory attachmentCategory = AttachmentCategory.fromString(type);
        if (attachmentCategory == null) return EMPTY_ATTACHMENT_ID;

        @NotNull var attachmentLocation = _this.iGun.getAttachmentLocation(_this.gunItem, attachmentCategory);
        if (ResourceTag.NULL_LOCATION.equals(attachmentLocation)) return EMPTY_ATTACHMENT_ID;
        else return attachmentCategory.toString();
    }

    protected static _LuaNbtAccessor getNbt(GunScriptApi _this) {
        return _this.nbtUtil;
    }

    protected static _LuaEntityAccessor getEntityUtil(GunScriptApi _this) {
        if (_this.entityAccessor == null) _this.entityAccessor = _LuaEntityAccessor.of(_this.livingShooter);
        return _this.entityAccessor;
    }

    protected static void setShooter(GunScriptApi _this, LivingEntity shooter) {
        _this.livingShooter = shooter;
        _this.iLivingShooter = ILivingShooterGetter.cgc$fromLivingEntity(_this.livingShooter);
    }

    protected static void setItemStack(GunScriptApi _this, ItemStack itemStack) {
        IGun iGun = IGunGetter.fromItemStack(itemStack);
        if (iGun == null) {
            CustomGun.LOGGER.warn("_GunScriptBackCompat: itemStack {} is not IGun", itemStack.toString());
            return;
        }
        _this.gunItem = itemStack;
        _this.iGun = iGun;
        _this.resetCache();
    }

    protected static void setPitchSupplier(GunScriptApi _this, Supplier<Float> pitchSupplier) {
        _this.pitchSupplier = pitchSupplier;
    }

    protected static void setYawSupplier(GunScriptApi _this, Supplier<Float> yawSupplier) {
        _this.yawSupplier = yawSupplier;
    }

    protected static LivingEntity getShooter(GunScriptApi _this) {
        return _this.livingShooter;
    }

    protected static ItemStack getItemStack(GunScriptApi _this) {
        return _this.gunItem;
    }

    protected static _AbstractGunItem getAbstractGunItem(GunScriptApi _this) {
        return new _AbstractGunItem(_this.iGun, _this.gunItem);
    }

    protected static void setHeatAmount(GunScriptApi _this, float amount) {
        _this.iGun.setHeatCount(_this.gunItem, amount);
    }

    protected static float getHeatAmount(GunScriptApi _this) {
        return _this.iGun.getHeatCount(_this.gunItem);
    }

    protected static boolean hasHeatData(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return false;

        GunData gunData = gunIndexInstance.getGunData();
        return gunData.getHeatData() != null;
    }

    protected static float getHeatMinRpm(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getMinRpmByHeat() : 0;
    }

    protected static float getHeatMaxRpm(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getMaxRpmByHeat() : 0;
    }

    protected static float getHeatMinInaccuracy(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getMinInaccuracyByHeat() : 0;
    }

    protected static float getHeatMaxInaccuracy(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getMaxInaccuracyByHeat() : 0;
    }

    protected static float getHeatMax(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getMaxHeat() : 0;
    }

    protected static float getHeatPerShot(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getHeatPerShot() : 0;
    }

    protected static boolean isOverheatLocked(GunScriptApi _this) {
        return _this.iGun.hasOverheatLock(_this.gunItem);
    }

    protected static void setOverheatLocked(GunScriptApi _this, boolean locked) {
        _this.iGun.setOverheatLock(_this.gunItem, locked);
    }

    protected static long getOverheatTime(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getOverheatLocktimeMs() : 0;
    }

    protected static long getCoolingDelay(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        return heatData != null ? heatData.getCoolingDelayMs() : 0;
    }

    protected static float calcHeatReduction(GunScriptApi _this, long heatTimestamp) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        @Nullable _HeatData heatData = gunData.getHeatData();
        if (heatData == null) return 0;
        else return ((float) (System.currentTimeMillis() - heatTimestamp) / 10_000f) * heatData.getCoolingSpeedMultiplier();
    }

    protected static int getBoltByInt(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return 0;

        GunData gunData = gunIndexInstance.getGunData();
        BoltType boltType = gunData.getBoltType();
        return boltType.getIndex();
    }

    protected static BoltType getBolt(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return null;

        GunData gunData = gunIndexInstance.getGunData();
        return gunData.getBoltType();
    }

    private static @Nullable _ChargingData getChargeData(GunScriptApi _this) {
        @Nullable GunIndexInstance gunIndexInstance = _this.gunIndexInstanceCache;
        if (gunIndexInstance == null) return null;

        GunData gunData = gunIndexInstance.getGunData();
        Map<FireModeType, _ChargingData> chargingData = gunData.getChargingData();
        return chargingData.get(_this.iGun.getFireModeType(_this.gunItem));
    }

    protected static void setDataHolder(GunScriptApi _this, ShooterProperty dataHolder) {
        if (_this.iLivingShooter == null) return;
        throw new IllegalStateException("_GunScriptBackCompat: Reject to set data holder, DO NOT USE setDataHolder");
    }

    protected static boolean useInventoryAmmo(GunScriptApi _this) {
        return _this.iGun.useInventoryAmmo(_this.gunItem);
    }

    protected static ShooterProperty getDataHolder(GunScriptApi _this) {
        if (_this.iLivingShooter == null) return null;
        return _this.iLivingShooter.cgc$getShooterProperty();
    }
}
