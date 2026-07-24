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
import xiao.customgun.core.resource.data.data.gun._FireModeAdjustData;

import java.util.Collection;

public final class ProneInaccuracyModifier extends AttachmentModifier<_SimpleModifierData, Float> {
    public static final ProneInaccuracyModifier INSTANCE = new ProneInaccuracyModifier();

    // --------IAttachmentModifier--------

    @Override
    public _SimpleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getProneInaccuracyModifier();
    }

    @Override
    public Float eval(Collection<_SimpleModifierData> modifiers, Float base) {
        return evalSimpleModifierData(modifiers, base);
    }

    // --------IGunModifier--------

    @Override
    public Float getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem, @NotNull GunData gunData) {
        var inaccuracy = gunData.getInaccuracyData();
        if (inaccuracy == null) return 2.5F;
        float base = inaccuracy.getProne();
        _FireModeAdjustData fireModeAdjust = gunData.getFireModeAdjustData().get(iGun.getFireModeType(gunItem));
        if (fireModeAdjust != null) base += fireModeAdjust.getOtherInaccuracy();
        return Math.max(base, 0);
    }
}
