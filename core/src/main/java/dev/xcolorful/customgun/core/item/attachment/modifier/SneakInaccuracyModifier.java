/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.attachment.modifier;

import dev.xcolorful.customgun.core.api.item.gun.modifier.ISneakInaccuracyModifier;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._SimpleModifierData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class SneakInaccuracyModifier extends AttachmentModifier<_SimpleModifierData, Float>
        implements ISneakInaccuracyModifier<AttachmentData> {
    public static final SneakInaccuracyModifier INSTANCE = new SneakInaccuracyModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable _SimpleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getSneakInaccuracyModifier();
    }

    @Override
    public Float eval(Collection<_SimpleModifierData> modifiers, Float base) {
        return evalSimpleModifierData(modifiers, base);
    }
}
