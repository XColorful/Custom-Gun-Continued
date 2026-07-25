/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.gun.modifier.IFireAspectModifier;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.attachment._FireAspectModifierData;

import java.util.Collection;

public final class FireAspectModifier extends AttachmentModifier<_FireAspectModifierData, _FireAspectModifierData>
        implements IFireAspectModifier<AttachmentData> {
    public static final FireAspectModifier INSTANCE = new FireAspectModifier();

    // --------IAttachmentModifier--------

    @Override
    public _FireAspectModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getFireAspectModifier();
    }

    @Override
    public _FireAspectModifierData eval(Collection<_FireAspectModifierData> modifiers, _FireAspectModifierData base) {
        // TODO: eval 不能复用父类函数 — _FireAspectModifierData 有 boolean 语义（OR/AND），非数值计算
        return base;
    }
}
