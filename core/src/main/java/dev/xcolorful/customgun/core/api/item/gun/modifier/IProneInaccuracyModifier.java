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
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IProneInaccuracyModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _SimpleModifierData, Float> {

    @Override
    default @Nullable Float getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                    @NotNull GunData gunData) {
        var inaccuracy = gunData.getInaccuracyData();
        if (inaccuracy == null) return 2.5F;
        float base = inaccuracy.getProne();
//        _FireModeAdjustData fireModeAdjust = gunData.getFireModeAdjustData().get(iGun.getFireModeType(gunItem));
//        if (fireModeAdjust != null) base += fireModeAdjust.getOtherInaccuracy();
        return Math.max(base, 0);
    }

    @Override
    default Float evalByScript(Float base, Float value, String scriptFunction) {
        return IGunModifier.evalSimpleModifierDataByScript(base, value, scriptFunction);
    }

    static @Nullable Float getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IProneInaccuracyModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, Float value) {
        cache.setValue(modifierHolder, IProneInaccuracyModifier.class, value);
    }
    static @NotNull Float evalByScript(GunScriptApi scriptApi, @NotNull Float value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.PRONE_INACCURACY, value);
    }
}
