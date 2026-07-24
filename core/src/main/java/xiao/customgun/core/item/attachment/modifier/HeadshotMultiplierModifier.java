/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.gun.modifier.IHeadshotMultiplierModifier;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment._SimpleModifierData;

import java.util.Collection;

public final class HeadshotMultiplierModifier extends AttachmentModifier<_SimpleModifierData, Float>
        implements IHeadshotMultiplierModifier<AttachmentData> {
    public static final HeadshotMultiplierModifier INSTANCE = new HeadshotMultiplierModifier();

    // --------IAttachmentModifier--------

    @Override
    public _SimpleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getHeadshotMultiplierModifier();
    }

    @Override
    public Float eval(Collection<_SimpleModifierData> modifiers, Float base) {
        return evalSimpleModifierData(modifiers, base);
    }
}
