/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.attachment.modifier;

import dev.xcolorful.customgun.core.api.item.gun.modifier.IFireAspectModifier;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._FireAspectModifierData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class FireAspectModifier extends AttachmentModifier<_FireAspectModifierData, Boolean>
        implements IFireAspectModifier<AttachmentData> {
    public static final FireAspectModifier INSTANCE = new FireAspectModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable _FireAspectModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getFireAspectModifier();
    }

    @Override
    public Boolean eval(Collection<_FireAspectModifierData> modifiers, Boolean base) {
        for (_FireAspectModifierData modifier : modifiers) {
            if (modifier.getIgniteEntity() || modifier.getIgniteBlock()) return true;
        }
        return base;
    }
}
