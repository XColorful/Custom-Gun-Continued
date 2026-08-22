/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.script.context;

import dev.xcolorful.customgun.core.api.entity.ILivingShooter;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.minecraft.TriBool;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.api.script.ScriptMethodType;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.function.Supplier;

public class GunScriptApi implements IGunScriptApi {

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
        return scriptMethodType.getFunction(this.scriptCache);
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

    @Deprecated(forRemoval = false) public _LuaNbtAccessor getNbt() {
        return this.nbtUtil;
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

    @Deprecated(forRemoval = false) public void shootOnce(boolean consumeAmmo) {
        _GunScriptBackCompat.shootOnce(this, consumeAmmo);
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
    @Deprecated(forRemoval = false) public LuaTable getScriptParam() {
        return _GunScriptBackCompat.getScriptParam(this);
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
}
