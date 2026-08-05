/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.attachment.modifier;

import dev.xcolorful.customgun.core.api.item.gun.modifier.IDamageCalculationModifier;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._SimpleModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun.bullet.damage._DistanceDamageData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class DamageCalculationModifier extends AttachmentModifier<_SimpleModifierData, List<_DistanceDamageData>>
        implements IDamageCalculationModifier<AttachmentData> {
    public static final DamageCalculationModifier INSTANCE = new DamageCalculationModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable _SimpleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getDamageCalculationModifier();
    }

    @Override
    public List<_DistanceDamageData> eval(Collection<_SimpleModifierData> modifiers, List<_DistanceDamageData> base) {
        List<_DistanceDamageData> result = new ArrayList<>(base.size());
        for (_DistanceDamageData entry : base) {
            _DistanceDamageData copy = new _DistanceDamageData();
            copy.setDistance(entry.getDistance());
            copy.setDamage(evalSimpleModifierData(modifiers, entry.getDamage()));
            result.add(copy);
        }
        return result;
    }
}
