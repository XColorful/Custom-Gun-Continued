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
import xiao.customgun.core.api.item.gun.FireSoundType;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.attachment._MuzzleModifierData;

import java.util.Collection;

public final class MuzzleModifier extends AttachmentModifier<_MuzzleModifierData, FireSoundType> {
    public static final MuzzleModifier INSTANCE = new MuzzleModifier();

    // --------IAttachmentModifier--------

    @Override
    public _MuzzleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getMuzzleModifier();
    }

    @Override
    public FireSoundType eval(Collection<_MuzzleModifierData> modifiers, FireSoundType base) {
        // TODO: eval 不能复用父类函数 — MuzzleModifier 的 K 是 _MuzzleModifierData（含 FireSoundType），非数值
        return base;
    }

    // --------IGunModifier--------

    @Override
    public FireSoundType getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem, @NotNull GunData gunData) {
        var fireSoundData = gunData.getFireSoundData();
        // TODO: FireSoundData → FireSoundType 映射
        return FireSoundType.NORMAL;
    }
}
