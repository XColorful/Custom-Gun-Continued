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
import dev.xcolorful.customgun.core.resource.data.data.attachment._RecoilDataModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun._RecoilData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IRecoilDataModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _RecoilDataModifierData, _RecoilData> {

    @Override
    default @Nullable _RecoilData getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                       @NotNull GunData gunData) {
        _RecoilData source = gunData.getRecoilData();
        // TODO copy
        _RecoilData result = source;
        return result;
    }

    static @Nullable _RecoilDataModifierData getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IRecoilDataModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, _RecoilDataModifierData value) {
        cache.setValue(modifierHolder, IRecoilDataModifier.class, value);
    }
    static @NotNull _RecoilDataModifierData evalByScript(GunScriptApi scriptApi, @NotNull _RecoilDataModifierData value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.RECOIL_DATA, value);
    }
}
