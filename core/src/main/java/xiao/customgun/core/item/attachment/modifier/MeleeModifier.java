/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.attachment.modifier;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.attachment._MeleeModifierData;

import java.util.Collection;

public final class MeleeModifier extends AttachmentModifier<_MeleeModifierData, _MeleeModifierData> {
    public static final MeleeModifier INSTANCE = new MeleeModifier();

    // --------IAttachmentModifier--------

    @Override
    public _MeleeModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getMeleeModifier();
    }

    @Override
    public _MeleeModifierData eval(Collection<_MeleeModifierData> modifiers, _MeleeModifierData base) {
        // TODO: eval 不能复用父类函数 — 近战有7个独立字段，各自需要独立的计算逻辑
        return base;
    }

    // --------IGunModifier--------

    @Override
    public _MeleeModifierData getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem, @NotNull GunData gunData) {
        // TODO: 从 gunData.getMeleeData() 构建 base _MeleeModifierData
        return new _MeleeModifierData();
    }
}
