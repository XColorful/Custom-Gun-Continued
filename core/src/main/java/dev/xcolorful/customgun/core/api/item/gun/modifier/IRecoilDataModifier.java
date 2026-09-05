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
import dev.xcolorful.customgun.core.resource.ResourcePojo;
import dev.xcolorful.customgun.core.resource.data.data.GunData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._RecoilDataModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun._RecoilData;
import dev.xcolorful.customgun.core.resource.data.data.gun.recoil._RecoilEntryData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface IRecoilDataModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _RecoilDataModifierData, _RecoilData> {

    @Override
    default @Nullable _RecoilData getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                       @NotNull GunData gunData) {
        _RecoilData result = new _RecoilData(); {
            _RecoilData source = gunData.getRecoilData();
            result.setPitchRecoils(_copyRecoilEntries(source.getPitchRecoils()));
            result.setYawRecoils(_copyRecoilEntries(source.getYawRecoils()));
        }
        return result;
    }
    private static List<_RecoilEntryData> _copyRecoilEntries(List<_RecoilEntryData> source) {
        List<_RecoilEntryData> result = new ArrayList<>(source.size());
        for (_RecoilEntryData entry : source) {
            _RecoilEntryData copy = new _RecoilEntryData();
            copy.setTime(entry.getTime());
            copy.setRange(entry.getRange().clone());
            result.add(copy);
        }
        return result;
    }

    static @Nullable _RecoilData getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IRecoilDataModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, _RecoilData value) {
        cache.setValue(modifierHolder, IRecoilDataModifier.class, value);
    }
    static @NotNull _RecoilData evalByScript(GunScriptApi scriptApi, @NotNull _RecoilData value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.RECOIL_DATA, value);
    }
}
