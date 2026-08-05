/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.attachment.modifier;

import dev.xcolorful.customgun.core.api.item.gun.FireSoundType;
import dev.xcolorful.customgun.core.api.item.gun.modifier.IMuzzleModifier;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._MuzzleModifierData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class MuzzleModifier extends AttachmentModifier<_MuzzleModifierData, FireSoundType>
        implements IMuzzleModifier<AttachmentData> {
    public static final MuzzleModifier INSTANCE = new MuzzleModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable _MuzzleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getMuzzleModifier();
    }

    @Override
    public FireSoundType eval(Collection<_MuzzleModifierData> modifiers, FireSoundType base) {
        if (modifiers.isEmpty()) return base;

        for (_MuzzleModifierData modifier : modifiers) {
            return modifier.getFireSoundType();
        }

        return base;
    }
}
