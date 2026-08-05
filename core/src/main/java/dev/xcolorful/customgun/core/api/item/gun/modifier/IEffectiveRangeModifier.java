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
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._SimpleModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet._BulletSkillData;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface IEffectiveRangeModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _SimpleModifierData, Float> {

    @Override
    default @Nullable Float getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                    @NotNull GunData gunData) {
        _BulletSkillData bulletSkill = gunData.getBulletData().getBulletSkillData();
        List<_DistanceDamageData> source = bulletSkill.getDamageCalculation();
        if (source.isEmpty()) return null;
        else return source.get(0).getDistance();
    }

    @Override
    default Float evalByScript(Float base, Float value, String scriptFunction) {
        return IGunModifier.evalSimpleModifierDataByScript(base, value, scriptFunction);
    }

    static @Nullable Float getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IEffectiveRangeModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, Float value) {
        cache.setValue(modifierHolder, IEffectiveRangeModifier.class, value);
    }
    static @NotNull Float evalByScript(GunScriptApi scriptApi, @NotNull Float value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.EFFECTIVE_RANGE, value);
    }
}
