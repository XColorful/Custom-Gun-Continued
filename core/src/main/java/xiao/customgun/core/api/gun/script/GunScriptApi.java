/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.gun.script;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.entity.ShooterProperty;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.BoltType;
import xiao.customgun.core.api.minecraft.TriBool;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.api.script.ScriptMethodType;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

import java.util.function.Supplier;

public class GunScriptApi {

    protected @Nullable ILivingShooter iLivingShooter;
    protected @Nullable LivingEntity livingShooter;
    protected IGun iGun;
    protected ItemStack gunItem;
    // ----Cache----
    protected @Nullable GunIndexInstance gunIndexInstanceCache;
    protected @Nullable LuaTable scriptCache;
    protected @Nullable LuaTable scriptParamsCache;

    private GunScriptApi() {
    }
    public static GunScriptApi of(ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                  @NotNull IGun iGun, @NotNull ItemStack gunItem) {
        GunScriptApi scriptApi = new GunScriptApi();
        scriptApi.iLivingShooter = iLivingShooter;
        scriptApi.livingShooter = livingShooter;
        scriptApi.iGun = iGun;
        scriptApi.gunItem = gunItem;
        scriptApi.resetCache();
        return scriptApi;
    }
    public static GunScriptApi of(@NotNull IGun iGun, @NotNull ItemStack gunItem) {
        return of(null, null, iGun, gunItem);
    }

    /**
     * @return 刷新后缓存是否可用
     */
    public boolean resetCache() {
        var gunLocation = iGun.getGunLocation(gunItem);
        this.gunIndexInstanceCache = ResourceApi.getGunIndexInstance(gunLocation);
        if (this.gunIndexInstanceCache == null) return false;

        this.scriptCache = this.gunIndexInstanceCache.getScript();
        this.scriptParamsCache = this.gunIndexInstanceCache.getScriptParams();
        if (this.scriptCache == null || this.scriptParamsCache == null) return false;

        return true;
    }

    /**
     * @return {@link #resetCache()}是否准备好
     */
    public boolean isCacheValid() {
        return this.gunIndexInstanceCache != null && this.scriptCache != null && this.scriptParamsCache != null;
    }

    public @Nullable LuaFunction getFunction(ScriptMethodType scriptMethodType) {
        if (this.scriptCache == null) return null;
        LuaValue function = this.scriptCache.get(scriptMethodType.getConstantName());
        if (function.isfunction()) return function.checkfunction();
        else return null;
    }

    /**
     * 执行函数，参数为{@link GunScriptApi}
     * @return 是否执行成功
     */
    public @NotNull TriBool simpleCall(ScriptMethodType scriptMethod) {
        @Nullable LuaFunction function = this.getFunction(scriptMethod);
        if (function == null) return TriBool.UNKNOWN;

        LuaValue result = function.call(
                CoerceJavaToLua.coerce(this)
        );

        if (!result.isboolean()) return TriBool.UNKNOWN;
        else return result.checkboolean() ? TriBool.TRUE : TriBool.FALSE;
    }

    // --------Getter & Setter--------

    public @Nullable ILivingShooter getILivingShooter() {
        return this.iLivingShooter;
    }
    public @Nullable LivingEntity getLivingShooter() {
        return this.livingShooter;
    }
    public IGun getIGun() {
        return this.iGun;
    }
    public ItemStack getGunItem() {
        return this.gunItem;
    }
    public @Nullable GunIndexInstance getGunIndexInstance() {
        return this.gunIndexInstanceCache;
    }
    public @Nullable LuaTable getScript() {
        return this.scriptCache;
    }
    public @Nullable LuaTable getScriptParams() {
        return this.scriptParamsCache;
    }

    public void setILivingShooter(ILivingShooter iLivingShooter) {
        this.iLivingShooter = iLivingShooter;
    }
    public void setLivingShooter(LivingEntity livingShooter) {
        this.livingShooter = livingShooter;
    }
    public void setIGun(IGun iGun) {
        this.iGun = iGun;
    }
    public void setGunItem(ItemStack gunItem) {
        this.gunItem = gunItem;
    }
    public void setGunIndexInstance(GunIndexInstance gunIndexInstance) {
        this.gunIndexInstanceCache = gunIndexInstance;
    }
    public void setScript(LuaTable script) {
        this.scriptCache = script;
    }
    public void setScriptParams(LuaTable scriptParams) {
        this.scriptParamsCache = scriptParams;
    }

    // --------Deprecated--------

    /**
     * @deprecated 如果参数不全，还要这个api干嘛用?
     */
    @Deprecated
    public static GunScriptApi empty() {
        return new GunScriptApi();
    }

    // --------Back compatibility--------

    protected Supplier<Float> pitchSupplier;
    protected Supplier<Float> yawSupplier;
    @Deprecated protected _LuaNbtAccessor nbtUtil;
    @Deprecated protected _LuaEntityAccessor entityAccessor;
    protected float shotDamageMultiplier = 1f;
    protected float projectileSpeedMultiplier = 1f;

    @Deprecated(forRemoval = false) public LuaValue getCachedProperty(String id) {
        return _GunScriptBackCompat.getCachedProperty(this, id);
    }
    @Deprecated(forRemoval = false) public void shootOnce(boolean consumeAmmo) {
        _GunScriptBackCompat.shootOnce(this, consumeAmmo);
    }
    @Deprecated(forRemoval = false) public void handleShootHeat() {
        _GunScriptBackCompat.handleShootHeat(this);
    }
    @Deprecated(forRemoval = false) public boolean reduceAmmoOnce() {
        return _GunScriptBackCompat.reduceAmmoOnce(this);
    }
    @Deprecated(forRemoval = false) public long getReloadTime() {
        return _GunScriptBackCompat.getReloadTime(this);
    }
    @Deprecated(forRemoval = false) public long getBoltTime() {
        return _GunScriptBackCompat.getBoltTime(this);
    }
    @Deprecated(forRemoval = false) public long getShootInterval() {
        return _GunScriptBackCompat.getShootInterval(this);
    }
    @Deprecated(forRemoval = false) public long getLastShootTimestamp() {
        return _GunScriptBackCompat.getLastShootTimestamp(this);
    }
    @Deprecated(forRemoval = false) public void adjustShootInterval(long alpha) {
        _GunScriptBackCompat.adjustShootInterval(this, alpha);
    }
    @Deprecated(forRemoval = false) public void adjustReloadTime(long alpha) {
        _GunScriptBackCompat.adjustReloadTime(this, alpha);
    }
    @Deprecated(forRemoval = false) public void adjustBoltTime(long alpha) {
        _GunScriptBackCompat.adjustBoltTime(this, alpha);
    }
    @Deprecated(forRemoval = false) public float getAimingProgress() {
        return _GunScriptBackCompat.getAimingProgress(this);
    }
    @Deprecated(forRemoval = false) public float getChargeProgress() {
        return _GunScriptBackCompat.getChargeProgress(this);
    }
    @Deprecated(forRemoval = false) public boolean hasChargeData() {
        return _GunScriptBackCompat.hasChargeData(this);
    }
    @Deprecated(forRemoval = false) public float getMaxCharge() {
        return _GunScriptBackCompat.getMaxCharge(this);
    }
    @Deprecated(forRemoval = false) public float getFireThreshold() {
        return _GunScriptBackCompat.getFireThreshold(this);
    }
    @Deprecated(forRemoval = false) public float getChargeRatio() {
        return _GunScriptBackCompat.getChargeRatio(this);
    }
    @Deprecated(forRemoval = false) public void setShotDamageMultiplier(float multiplier) {
        _GunScriptBackCompat.setShotDamageMultiplier(this, multiplier);
    }
    @Deprecated(forRemoval = false) public float getShotDamageMultiplier() {
        return _GunScriptBackCompat.getShotDamageMultiplier(this);
    }
    @Deprecated(forRemoval = false) public void setProjectileSpeedMultiplier(float multiplier) {
        _GunScriptBackCompat.setProjectileSpeedMultiplier(this, multiplier);
    }
    @Deprecated(forRemoval = false) public float getProjectileSpeedMultiplier() {
        return _GunScriptBackCompat.getProjectileSpeedMultiplier(this);
    }
    @Deprecated(forRemoval = false) public int getReloadStateType() {
        return _GunScriptBackCompat.getReloadStateType(this);
    }
    @Deprecated(forRemoval = false) public int getFireMode() {
        return _GunScriptBackCompat.getFireMode(this);
    }
    @Deprecated(forRemoval = false) public boolean isShootingNeedConsumeAmmo() {
        return _GunScriptBackCompat.isShootingNeedConsumeAmmo(this);
    }
    @Deprecated(forRemoval = false) public boolean isReloadingNeedConsumeAmmo() {
        return _GunScriptBackCompat.isReloadingNeedConsumeAmmo(this);
    }
    @Deprecated(forRemoval = false) public int getNeededAmmoAmount() {
        return _GunScriptBackCompat.getNeededAmmoAmount(this);
    }
    @Deprecated(forRemoval = false) public int getAmmoAmount() {
        return _GunScriptBackCompat.getAmmoAmount(this);
    }
    @Deprecated(forRemoval = false) public int getMaxAmmoCount() {
        return _GunScriptBackCompat.getMaxAmmoCount(this);
    }
    @Deprecated(forRemoval = false) public int getMagExtentLevel() {
        return _GunScriptBackCompat.getMagExtentLevel(this);
    }
    @Deprecated(forRemoval = false) public int consumeAmmoFromPlayer(int neededAmount) {
        return _GunScriptBackCompat.consumeAmmoFromPlayer(this, neededAmount);
    }
    @Deprecated(forRemoval = false) public boolean hasAmmoToConsume() {
        return _GunScriptBackCompat.hasAmmoToConsume(this);
    }
    @Deprecated(forRemoval = false) public int putAmmoInMagazine(int amount) {
        return _GunScriptBackCompat.putAmmoInMagazine(this, amount);
    }
    @Deprecated(forRemoval = false) public int removeAmmoFromMagazine(int amount) {
        return _GunScriptBackCompat.removeAmmoFromMagazine(this, amount);
    }
    @Deprecated(forRemoval = false) public int getAmmoCountInMagazine() {
        return _GunScriptBackCompat.getAmmoCountInMagazine(this);
    }
    @Deprecated(forRemoval = false) public boolean hasAmmoInBarrel() {
        return _GunScriptBackCompat.hasAmmoInBarrel(this);
    }
    @Deprecated(forRemoval = false) public void setAmmoInBarrel(boolean ammoInBarrel) {
        _GunScriptBackCompat.setAmmoInBarrel(this, ammoInBarrel);
    }
    @Deprecated(forRemoval = false) public void cacheScriptData(LuaValue luaValue) {
        _GunScriptBackCompat.cacheScriptData(this, luaValue);
    }
    @Deprecated(forRemoval = false) public LuaValue getCachedScriptData() {
        return _GunScriptBackCompat.getCachedScriptData(this);
    }
    @Deprecated(forRemoval = false) public LuaTable getScriptParam() {
        return _GunScriptBackCompat.getScriptParam(this);
    }
    @Deprecated(forRemoval = false) public void safeAsyncTask(LuaValue value, long delayMs, long periodMs, int cycles) {
        _GunScriptBackCompat.safeAsyncTask(this, value, delayMs, periodMs, cycles);
    }
    @Deprecated(forRemoval = false) public long getCurrentTimestamp() {
        return _GunScriptBackCompat.getCurrentTimestamp(this);
    }
    @Deprecated(forRemoval = false) public String getAttachment(String type) {
        return _GunScriptBackCompat.getAttachment(this, type);
    }
    @Deprecated(forRemoval = false) public _LuaNbtAccessor getNbt() {
        return _GunScriptBackCompat.getNbt(this);
    }
    @Deprecated(forRemoval = false) public _LuaEntityAccessor getEntityUtil() {
        return _GunScriptBackCompat.getEntityUtil(this);
    }
    @Deprecated(forRemoval = false) public void setShooter(LivingEntity shooter) {
        _GunScriptBackCompat.setShooter(this, shooter);
    }
    @Deprecated(forRemoval = false) public void setItemStack(ItemStack itemStack) {
        _GunScriptBackCompat.setItemStack(this, itemStack);
    }
    @Deprecated(forRemoval = false) public LivingEntity getShooter() {
        return _GunScriptBackCompat.getShooter(this);
    }
    @Deprecated(forRemoval = false) public ItemStack getItemStack() {
        return _GunScriptBackCompat.getItemStack(this);
    }
    @Deprecated(forRemoval = false) public _AbstractGunItem getAbstractGunItem() {
        return _GunScriptBackCompat.getAbstractGunItem(this);
    }
    @Deprecated(forRemoval = false) public void setHeatAmount(float amount) {
        _GunScriptBackCompat.setHeatAmount(this, amount);
    }
    @Deprecated(forRemoval = false) public float getHeatAmount() {
        return _GunScriptBackCompat.getHeatAmount(this);
    }
    @Deprecated(forRemoval = false) public boolean hasHeatData() {
        return _GunScriptBackCompat.hasHeatData(this);
    }
    @Deprecated(forRemoval = false) public float getHeatMinRpm() {
        return _GunScriptBackCompat.getHeatMinRpm(this);
    }
    @Deprecated(forRemoval = false) public float getHeatMaxRpm() {
        return _GunScriptBackCompat.getHeatMaxRpm(this);
    }
    @Deprecated(forRemoval = false) public float getHeatMinInaccuracy() {
        return _GunScriptBackCompat.getHeatMinInaccuracy(this);
    }
    @Deprecated(forRemoval = false) public float getHeatMaxInaccuracy() {
        return _GunScriptBackCompat.getHeatMaxInaccuracy(this);
    }
    @Deprecated(forRemoval = false) public float getHeatMax() {
        return _GunScriptBackCompat.getHeatMax(this);
    }
    @Deprecated(forRemoval = false) public float getHeatPerShot() {
        return _GunScriptBackCompat.getHeatPerShot(this);
    }
    @Deprecated(forRemoval = false) public boolean isOverheatLocked() {
        return _GunScriptBackCompat.isOverheatLocked(this);
    }
    @Deprecated(forRemoval = false) public void setOverheatLocked(boolean locked) {
        _GunScriptBackCompat.setOverheatLocked(this, locked);
    }
    @Deprecated(forRemoval = false) public long getOverheatTime() {
        return _GunScriptBackCompat.getOverheatTime(this);
    }
    @Deprecated(forRemoval = false) public long getCoolingDelay() {
        return _GunScriptBackCompat.getCoolingDelay(this);
    }
    @Deprecated(forRemoval = false) public float calcHeatReduction(long heatTimestamp) {
        return _GunScriptBackCompat.calcHeatReduction(this, heatTimestamp);
    }
    @Deprecated(forRemoval = false) public int getBoltByInt() {
        return _GunScriptBackCompat.getBoltByInt(this);
    }
    @Deprecated(forRemoval = false) public BoltType getBolt() {
        return _GunScriptBackCompat.getBolt(this);
    }
    @Deprecated(forRemoval = true) public void setDataHolder(ShooterProperty dataHolder) {
        _GunScriptBackCompat.setDataHolder(this, dataHolder);
    }
    @Deprecated(forRemoval = false) public boolean useInventoryAmmo() {
        return _GunScriptBackCompat.useInventoryAmmo(this);
    }
    @Deprecated(forRemoval = false) public ShooterProperty getDataHolder() {
        return _GunScriptBackCompat.getDataHolder(this);
    }
}
