/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.attachment.modifier;

import dev.xcolorful.customgun.core.api.item.gun.modifier.IMeleeModifier;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._MeleeModifierData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class MeleeModifier extends AttachmentModifier<_MeleeModifierData, _MeleeModifierData>
        implements IMeleeModifier<AttachmentData> {
    public static final MeleeModifier INSTANCE = new MeleeModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable _MeleeModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getMeleeModifier();
    }

    @Override
    public _MeleeModifierData eval(Collection<_MeleeModifierData> modifiers, _MeleeModifierData base) {
        if (modifiers.isEmpty()) return base;

        _MeleeModifierData result = new _MeleeModifierData();
//        List<_SimpleModifierData> meleeDamageModifiers = new ArrayList<>();
//        List<_SimpleModifierData> meleeDistanceModifiers = new ArrayList<>();
//        List<_SimpleModifierData> rangeAngleModifiers = new ArrayList<>();
//
//        List<_SimpleModifierData> damageDelaySecondsModifiers = new ArrayList<>();
//        List<_SimpleModifierData> baseCooldownModifiers = new ArrayList<>();
//
//        List<_SimpleModifierData> knockbackStrengthModifiers = new ArrayList<>();

        for (_MeleeModifierData modifier : modifiers) {
            result.setMeleeDamage(modifier.getMeleeDamage());
            result.setMeleeDamage(modifier.getMeleeDamage());
            result.setRangeAngle(modifier.getRangeAngle());

            result.setDamageDelaySeconds(modifier.getDamageDelaySeconds());
            result.setExtraCooldown(modifier.getExtraCooldown());

            result.setKnockbackStrength(modifier.getKnockbackStrength());
            result.setTargetEffect(modifier.getTargetEffect());
            return result;
        }

        return base;
    }
}
