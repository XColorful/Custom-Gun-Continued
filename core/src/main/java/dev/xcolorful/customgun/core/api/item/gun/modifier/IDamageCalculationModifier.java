/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.gun.modifier;

import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.gun.script.GunScriptApi;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.config.SyncConfig;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._SimpleModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun._FireModeAdjustData;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet._BulletSkillData;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface IDamageCalculationModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _SimpleModifierData, List<_DistanceDamageData>> {

    @Override
    default @Nullable List<_DistanceDamageData> getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                         @NotNull GunData gunData) {
        _BulletSkillData bulletSkill = gunData.getBulletData().getBulletSkillData();
        List<_DistanceDamageData> source = bulletSkill.getDamageCalculation();
        _FireModeAdjustData fireModeAdjust = gunData.getFireModeAdjustData().get(iGun.getFireModeType(gunItem));
        float fireAdjustDamage = fireModeAdjust != null ? fireModeAdjust.getDamage() : 0;
        float multiplier = SyncConfig.DAMAGE_BASE_MULTIPLIER.get().floatValue();

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
