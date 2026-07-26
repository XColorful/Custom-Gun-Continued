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
import org.luaj.vm2.LuaTable;
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

public class GunScriptApi {

    public @Nullable ILivingShooter iLivingShooter;
    public @Nullable LivingEntity livingShooter;
    public IGun iGun;
    public ItemStack gunItem;
    // ----Cache----
    public @Nullable GunIndexInstance gunIndexInstanceCache;
    public @Nullable LuaTable scriptCache;
    public @Nullable LuaTable scriptParamsCache;

    private GunScriptApi() {
    }
    public static GunScriptApi of(ILivingShooter iLivingShooter, LivingEntity livingShooter,
                                  @NotNull IGun iGun, @NotNull ItemStack gunItem) {
        GunScriptApi scriptApi = new GunScriptApi();
        scriptApi.iLivingShooter = iLivingShooter;
        scriptApi.livingShooter = livingShooter;
        scriptApi.iGun = iGun;
        scriptApi.gunItem = gunItem;
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
}
