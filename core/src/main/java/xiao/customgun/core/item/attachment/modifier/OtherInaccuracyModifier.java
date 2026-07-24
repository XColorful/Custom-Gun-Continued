/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.gun.modifier.IOtherInaccuracyModifier;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment._SimpleModifierData;

import java.util.Collection;

public final class OtherInaccuracyModifier extends AttachmentModifier<_SimpleModifierData, Float>
        implements IOtherInaccuracyModifier<AttachmentData> {
    public static final OtherInaccuracyModifier INSTANCE = new OtherInaccuracyModifier();

    // --------IAttachmentModifier--------

    @Override
    public _SimpleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getOtherInaccuracyModifier();
    }

    @Override
    public Float eval(Collection<_SimpleModifierData> modifiers, Float base) {
        return evalSimpleModifierData(modifiers, base);
    }
}
