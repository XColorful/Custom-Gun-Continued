/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun.modifier;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.config.SyncConfig;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.api.gun.script.GunScriptApi;
import xiao.customgun.core.resource.data.data.attachment._SimpleModifierData;
import xiao.customgun.core.resource.data.data.gun._FireModeAdjustData;

public interface IHeadshotMultiplierModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _SimpleModifierData, Float> {

    @Override
    default @Nullable Float getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                    @NotNull GunData gunData) {
        var bulletSkill = gunData.getBulletData().getBulletSkillData();
        float base = bulletSkill != null ? bulletSkill.getHeadshotMultiplier() : 0;
        _FireModeAdjustData fireModeAdjust = gunData.getFireModeAdjustData().get(iGun.getFireModeType(gunItem));
        if (fireModeAdjust != null) base += fireModeAdjust.getHeadshotMultiplier();
        base *= SyncConfig.HEAD_SHOT_BASE_MULTIPLIER.get();
        return base;
    }

    @Override
    default Float evalByScript(Float base, Float value, String scriptFunction) {
        return IGunModifier.evalSimpleModifierDataByScript(base, value, scriptFunction);
    }

    static @Nullable Float getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IHeadshotMultiplierModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, Float value) {
        cache.setValue(modifierHolder, IHeadshotMultiplierModifier.class, value);
    }
    static @NotNull Float evalByScript(GunScriptApi scriptApi, @NotNull Float value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.HEADSHOT_MULTIPLIER, value);
    }
}
