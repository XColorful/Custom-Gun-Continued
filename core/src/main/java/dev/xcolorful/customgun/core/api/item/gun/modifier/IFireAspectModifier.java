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
import dev.xcolorful.customgun.core.resource.data.data.attachment._FireAspectModifierData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFireAspectModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _FireAspectModifierData, Boolean> {

    @Override
    default @Nullable Boolean getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                       @NotNull GunData gunData) {
        return gunData.getBulletData().isFireAspect();
    }

    static @Nullable Boolean getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IFireAspectModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, _FireAspectModifierData value) {
        cache.setValue(modifierHolder, IFireAspectModifier.class, value);
    }
    static @NotNull Boolean evalByScript(GunScriptApi scriptApi, @NotNull Boolean value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.FIRE_ASPECT, value);
    }
}
