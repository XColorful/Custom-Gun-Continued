/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.gun.modifier.IRecoilDataModifier;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment._RecoilDataModifierData;

import java.util.Collection;

public final class RecoilDataModifier extends AttachmentModifier<_RecoilDataModifierData, _RecoilDataModifierData>
        implements IRecoilDataModifier<AttachmentData> {
    public static final RecoilDataModifier INSTANCE = new RecoilDataModifier();

    // --------IAttachmentModifier--------

    @Override
    public _RecoilDataModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getRecoilDataModifier();
    }

    @Override
    public _RecoilDataModifierData eval(Collection<_RecoilDataModifierData> modifiers, _RecoilDataModifierData base) {
        // TODO: eval 不能复用父类函数 — Pitch/Yaw 各有独立的 _SimpleModifierData，需要分别计算
        return base;
    }
}
