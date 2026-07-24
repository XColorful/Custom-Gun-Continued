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
import xiao.customgun.core.config.SyncConfig;
import xiao.customgun.core.resource.data.data.AttachmentData;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.attachment._SimpleModifierData;
import xiao.customgun.core.resource.data.data.gun._BulletData;
import xiao.customgun.core.resource.data.data.gun._FireModeAdjustData;

import java.util.Collection;

public final class ArmorIgnoreModifier extends AttachmentModifier<_SimpleModifierData, Float> {
    public static final ArmorIgnoreModifier INSTANCE = new ArmorIgnoreModifier();

    // --------IAttachmentModifier--------

    @Override
    public _SimpleModifierData getModifier(@NotNull AttachmentData pojo) {
        return pojo.getArmorIgnorePercentModifier();
    }

    @Override
    public Float eval(Collection<_SimpleModifierData> modifiers, Float base) {
        return evalSimpleModifierData(modifiers, base);
    }

    // --------IGunModifier--------

    @Override
    public Float getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem, @NotNull GunData gunData) {
        _BulletData bulletData = gunData.getBulletData();
        _FireModeAdjustData fireModeAdjust = gunData.getFireModeAdjustData().get(iGun.getFireModeType(gunItem));
        float base = 0;
        if (fireModeAdjust != null) base += fireModeAdjust.getArmorIgnorePercent();
        // TODO ExtraDamage
        base *= SyncConfig.ARMOR_IGNORE_BASE_MULTIPLIER.get();
        return base;
    }
}
