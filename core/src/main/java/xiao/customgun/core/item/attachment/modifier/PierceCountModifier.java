/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.gun.modifier.IPierceCountModifier;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment._SimpleModifierData;

import java.util.Collection;

public final class PierceCountModifier extends AttachmentModifier<_SimpleModifierData, Integer>
        implements IPierceCountModifier<AttachmentData> {
    public static final PierceCountModifier INSTANCE = new PierceCountModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable _SimpleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getPierceCountModifier();
    }

    @Override
    public Integer eval(Collection<_SimpleModifierData> modifiers, Integer base) {
        Float result = evalSimpleModifierData(modifiers, base.floatValue());
        return result != null ? Math.round(result) : base;
    }
}
