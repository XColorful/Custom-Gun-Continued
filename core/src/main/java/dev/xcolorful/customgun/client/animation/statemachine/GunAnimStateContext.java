/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.animation.statemachine;

import dev.xcolorful.customgun.client.api.entity.ILocalShooter;
import dev.xcolorful.customgun.client.api.entity.shooter.ILocalShooterGetter;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.api.script.context.IClientGunScriptApi;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.api.script.context._LuaNbtAccessor;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class GunAnimStateContext extends ItemAnimStateContext implements IClientGunScriptApi {

    private ItemStack currentGunItem;
    private @Nullable IGun iGun;
    private @Nullable GunDisplayInstance gunDisplayInstance;
    private @Nullable GunIndexInstance gunIndexInstance;
    private GunData gunData;
    private float walkDistAnchor = 0f;
    private _LuaNbtAccessor nbtUtil;

    public GunAnimStateContext() {
    }

    // --------Setter--------

    /**
     * 状态机脚本请不要调用此方法。此方法用于状态机更新时设置当前的物品对象
     */
    @ApiStatus.Internal
    public void setCurrentGunItem(ItemStack gunItem) {
        this.currentGunItem = gunItem;
        this.iGun = IGunGetter.fromItemStack(gunItem);

        if (this.iGun != null) {
            @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
            this.gunDisplayInstance = gunDisplayInstance;

            var gunLocation = this.iGun.getGunLocation(gunItem);
            @Nullable GunIndexInstance gunIndexInstance = ResourceApi.getGunIndexInstance(gunLocation);
            this.gunIndexInstance = gunIndexInstance;
            this.gunData = gunIndexInstance != null ? gunIndexInstance.getGunData() : null;
        }

        this.nbtUtil = _LuaNbtAccessor.of(this.currentGunItem);
    }

    // --------IGunScriptContextAccess--------

    @Override public @Nullable IGun getIGun() {
        return this.iGun;
    }
    @Override public ItemStack getGunItem() {
        return this.currentGunItem;
    }
    @Override public @Nullable GunIndexInstance getGunIndexInstance() {
        return this.gunIndexInstance;
    }
    @Override public @Nullable GunDisplayInstance getGunDisplayInstance() {
        return this.gunDisplayInstance;
    }

    @Deprecated(forRemoval = false)
    @Override public float getWalkDistAnchor() {
        return this.walkDistAnchor;
    }

    @Deprecated(forRemoval = false)
    @Override public @Nullable _LuaNbtAccessor getNbt() {
        return this.nbtUtil;
    }

    @Deprecated(forRemoval = false)
    @Override public void setWalkDistAnchor(float value) {
        this.walkDistAnchor = value;
    }

    // --------IClientGunScriptContextAccess--------

    @Override public @Nullable ILocalShooter getILocalShooter() {
        @Nullable LocalPlayer localShooter = this.getLocalShooter();
        return localShooter != null ? ILocalShooterGetter.fromLocalPlayer(localShooter) : null;
    }
    @Override public @Nullable LocalPlayer getLocalShooter() {
        return Minecraft.getInstance().player;
    }

    @Override public @Nullable Entity getCameraShooter() {
        return Minecraft.getInstance().getCameraEntity();
    }
}
