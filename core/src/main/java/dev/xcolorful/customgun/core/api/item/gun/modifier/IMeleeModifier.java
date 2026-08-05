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
import dev.xcolorful.customgun.core.resource.data.data.attachment._MeleeModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun._MeleeData;
import dev.xcolorful.customgun.core.resource.data.data.gun.melee._DefaultMeleeData;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public interface IMeleeModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _MeleeModifierData, _MeleeModifierData> {

    @Override
    default @Nullable _MeleeModifierData getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                  @NotNull GunData gunData) {
        _MeleeData source = gunData.getMeleeData();
        _DefaultMeleeData defaultMeleeData = source.getDefaultMeleeData();
        _MeleeModifierData base = new _MeleeModifierData();

        base.setMeleeDamage(defaultMeleeData.getMeleeDamage());
        base.setMeleeDistance(source.getGunBaseLength());
        base.setRangeAngle(defaultMeleeData.getRangeAngle());

        base.setDamageDelaySeconds(defaultMeleeData.getDamageDelaySeconds());
        base.setExtraCooldown(defaultMeleeData.getBaseCooldown());

        base.setKnockbackStrength(defaultMeleeData.getKnockbackStrength());
        base.setTargetEffect(new ArrayList<>());
        return base;
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
