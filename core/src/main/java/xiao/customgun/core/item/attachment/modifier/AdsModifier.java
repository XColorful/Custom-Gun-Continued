/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.gun.modifier.IAdsModifier;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment._SimpleModifierData;

import java.util.Collection;

public final class AdsModifier extends AttachmentModifier<_SimpleModifierData, Float>
        implements IAdsModifier<AttachmentData> {
    public static final AdsModifier INSTANCE = new AdsModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable _SimpleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getAdsModifier();
    }

    @Override
    public Float eval(Collection<_SimpleModifierData> modifiers, Float base) {
        return evalSimpleModifierData(modifiers, base);
    }
}
