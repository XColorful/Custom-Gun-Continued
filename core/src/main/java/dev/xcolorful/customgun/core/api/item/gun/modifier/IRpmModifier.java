/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.gun.modifier;

import dev.xcolorful.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import dev.xcolorful.customgun.core.api.gun.script.context.GunScriptApi;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._SimpleModifierData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRpmModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _SimpleModifierData, Integer> {

    @Override
    default @Nullable Integer getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                      @NotNull GunData gunData) {
        int rpm = gunData.getRpm();
        var fireModeAdjust = gunData.getFireModeAdjustData().get(iGun.getFireModeType(gunItem));
        if (fireModeAdjust != null) rpm = fireModeAdjust.getRpm();
        if (rpm <= 0) rpm = 300;
        return rpm;
    }

    static @Nullable Integer getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IRpmModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, Integer value) {
        cache.setValue(modifierHolder, IRpmModifier.class, value);
    }
    static @NotNull Integer evalByScript(GunScriptApi scriptApi, @NotNull Integer value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.RPM, value);
    }
}
