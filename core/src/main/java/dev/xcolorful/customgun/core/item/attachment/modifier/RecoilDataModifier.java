/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.item.attachment.modifier;

import dev.xcolorful.customgun.core.api.item.gun.modifier.IRecoilDataModifier;
import dev.xcolorful.customgun.core.resource.data.data.AttachmentData;
import dev.xcolorful.customgun.core.resource.data.data.attachment._RecoilDataModifierData;
import dev.xcolorful.customgun.core.resource.data.data.gun._RecoilData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class RecoilDataModifier extends AttachmentModifier<_RecoilDataModifierData, _RecoilData>
        implements IRecoilDataModifier<AttachmentData> {
    public static final RecoilDataModifier INSTANCE = new RecoilDataModifier();

    // --------IAttachmentModifier--------

    @Override
    public @Nullable _RecoilDataModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getRecoilDataModifier();
    }

    @Override
    public _RecoilData eval(Collection<_RecoilDataModifierData> modifiers, _RecoilData base) {
        // TODO
        return base;
    }
}
