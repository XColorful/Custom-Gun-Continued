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
import xiao.customgun.core.resource.data.data.attachment._MeleeModifierData;

public interface IMeleeModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _MeleeModifierData, _MeleeModifierData> {

    @Override
    default @Nullable _MeleeModifierData getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                  @NotNull GunData gunData) {
        // TODO: from gunData.getMeleeData() — build a copy
        return new _MeleeModifierData();
    }

    static @Nullable _MeleeModifierData getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IMeleeModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, _MeleeModifierData value) {
        cache.setValue(modifierHolder, IMeleeModifier.class, value);
    }
    static @NotNull _MeleeModifierData evalByScript(GunScriptApi scriptApi, @NotNull _MeleeModifierData value) {
        return scriptApi.getIGun().evalByScript(scriptApi.getGunItem(), scriptApi, GunModifierType.MELEE, value);
    }
}
