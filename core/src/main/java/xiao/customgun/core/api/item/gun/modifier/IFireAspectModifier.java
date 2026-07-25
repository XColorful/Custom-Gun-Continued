/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item.gun.modifier;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.resource.data.data.attachment._FireAspectModifierData;

public interface IFireAspectModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _FireAspectModifierData, _FireAspectModifierData> {

    @Override
    default @Nullable _FireAspectModifierData getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                       @NotNull GunData gunData) {
        var base = new _FireAspectModifierData();
        base.setIgniteEntity(gunData.getBulletData().isFireAspect());
        // TODO igniteBlock
        return base;
    }
}
