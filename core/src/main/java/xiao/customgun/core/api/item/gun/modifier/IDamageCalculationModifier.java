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
import xiao.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;

import java.util.ArrayList;
import java.util.List;

/**
 * Damage involves distance→damage decay curve (List&lt;_DistanceDamageData&gt;).
 * In TACZ, modifiers apply to each distance pair's damage independently:
 * for each (distance, damage) pair, damage is modified by all attachment modifiers.
 *
 * V is List&lt;_DistanceDamageData&gt; — getBase returns a COPY from BulletSkillData,
 * eval clones the list and applies modifications to each entry's damage separately.
 */
public interface IDamageCalculationModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _SimpleModifierData, List<_DistanceDamageData>> {

    @Override
    default @Nullable List<_DistanceDamageData> getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                         @NotNull GunData gunData) {
        var bulletSkill = gunData.getBulletData().getBulletSkillData();
        if (bulletSkill == null) return null;
        List<_DistanceDamageData> source = bulletSkill.getDamageCalculation();
        if (source == null) return null;

        _FireModeAdjustData fireModeAdjust = gunData.getFireModeAdjustData().get(iGun.getFireModeType(gunItem));
        float fireAdjustDamage = fireModeAdjust != null ? fireModeAdjust.getDamage() : 0;
        float multiplier = SyncConfig.DAMAGE_BASE_MULTIPLIER.get().floatValue();

        // Build a COPY so eval doesn't pollute BulletSkillData's original list
        List<_DistanceDamageData> result = new ArrayList<>(source.size());
        for (_DistanceDamageData entry : source) {
            _DistanceDamageData copy = new _DistanceDamageData();
            copy.setDistance(entry.getDistance());
            copy.setDamage((entry.getDamage() + fireAdjustDamage) * multiplier);
            result.add(copy);
        }
        return result;
    }

    static @Nullable List<_DistanceDamageData> getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IDamageCalculationModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, List<_DistanceDamageData> value) {
        cache.setValue(modifierHolder, IDamageCalculationModifier.class, value);
    }
    static @NotNull List<_DistanceDamageData> evalByScript(GunScriptApi scriptApi, @NotNull List<_DistanceDamageData> value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.DAMAGE_CALCULATION, value);
    }
}
