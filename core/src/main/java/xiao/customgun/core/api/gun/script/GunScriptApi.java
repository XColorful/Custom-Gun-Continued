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
import xiao.customgun.core.api.entity.ILivingShooter;
import xiao.customgun.core.api.item.IGun;

public class GunScriptApi {

    public ILivingShooter iLivingShooter;
    public LivingEntity livingShooter;
    public IGun iGun;
    public ItemStack gunItem;

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

    // --------Getter & Setter--------

    public ILivingShooter getILivingShooter() {
        return this.iLivingShooter;
    }
    public LivingEntity getLivingShooter() {
        return this.livingShooter;
    }
    public IGun getIGun() {
        return this.iGun;
    }
    public ItemStack getGunItem() {
        return this.gunItem;
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

    // --------Deprecated--------

    /**
     * @deprecated 如果参数不全，还要这个api干嘛用?
     */
    @Deprecated
    public static GunScriptApi empty() {
        return new GunScriptApi();
    }
}
