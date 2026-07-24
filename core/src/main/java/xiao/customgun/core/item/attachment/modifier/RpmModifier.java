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
import xiao.customgun.core.resource.data.data.attachment._SimpleModifierData;

import java.util.Collection;

public final class RpmModifier extends AttachmentModifier<_SimpleModifierData, Integer> {
    public static final RpmModifier INSTANCE = new RpmModifier();

    // --------IAttachmentModifier--------

    @Override
    public _SimpleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getRpmModifier();
    }

    @Override
    public Integer eval(Collection<_SimpleModifierData> modifiers, Integer base) {
        Float result = evalSimpleModifierData(modifiers, base.floatValue());
        return result != null ? Math.round(result) : base;
    }

    // --------IGunModifier--------

    @Override
    public Integer getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem, @NotNull GunData gunData) {
        int rpm = gunData.getRpm();
        var fireModeAdjust = gunData.getFireModeAdjustData().get(iGun.getFireModeType(gunItem));
        if (fireModeAdjust != null) rpm += fireModeAdjust.getRpm();
        if (rpm <= 0) rpm = 300;
        return rpm;
    }
}
