/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.gun.modifier.IRecoilDataModifier;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment._RecoilDataModifierData;
import xiao.customgun.core.resource.data.data.gun._RecoilData;

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
