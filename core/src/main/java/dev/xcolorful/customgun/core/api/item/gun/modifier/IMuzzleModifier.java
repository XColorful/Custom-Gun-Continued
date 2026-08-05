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
import dev.xcolorful.customgun.core.api.item.gun.FireSoundType;
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._MuzzleModifierData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMuzzleModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _MuzzleModifierData, FireSoundType> {

    @Override
    default @Nullable FireSoundType getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                            @NotNull GunData gunData) {
        return FireSoundType.NORMAL;
    }

    static @Nullable FireSoundType getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IMuzzleModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, FireSoundType value) {
        cache.setValue(modifierHolder, IMuzzleModifier.class, value);
    }
    static @NotNull FireSoundType evalByScript(GunScriptApi scriptApi, @NotNull FireSoundType value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.MUZZLE, value);
    }
}
