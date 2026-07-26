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
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.api.gun.script.GunScriptApi;
import xiao.customgun.core.resource.data.data.attachment._RecoilDataModifierData;

public interface IRecoilDataModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _RecoilDataModifierData, _RecoilDataModifierData> {

    @Override
    default @Nullable _RecoilDataModifierData getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                       @NotNull GunData gunData) {
        // TODO: from gunData.getRecoilData() — build a copy
        return new _RecoilDataModifierData();
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
