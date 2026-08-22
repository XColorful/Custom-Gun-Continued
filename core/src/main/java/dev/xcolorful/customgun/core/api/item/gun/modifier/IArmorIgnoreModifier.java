/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.gun.modifier;

import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.script.context.GunScriptApi;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.config.SyncConfig;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._SimpleModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun._FireModeAdjustData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IArmorIgnoreModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _SimpleModifierData, Float> {

    @Override
    default @Nullable Float getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                    @NotNull GunData gunData) {
        var bulletSkill = gunData.getBulletData().getBulletSkillData();
        float base = bulletSkill != null ? bulletSkill.getArmorIgnorePercent() : 0;
        _FireModeAdjustData fireModeAdjust = gunData.getFireModeAdjustData().get(iGun.getFireModeType(gunItem));
        if (fireModeAdjust != null) base += fireModeAdjust.getArmorIgnorePercent();
        base *= SyncConfig.ARMOR_IGNORE_BASE_MULTIPLIER.get();
        return base;
    }

    @Override
    default Float evalByScript(Float base, Float value, String scriptFunction) {
        return IGunModifier.evalSimpleModifierDataByScript(base, value, scriptFunction);
    }

    static @Nullable Float getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IArmorIgnoreModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, Float value) {
        cache.setValue(modifierHolder, IArmorIgnoreModifier.class, value);
    }
    static @NotNull Float evalByScript(GunScriptApi scriptApi, @NotNull Float value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.ARMOR_IGNORE_PERCENT, value);
    }
}
