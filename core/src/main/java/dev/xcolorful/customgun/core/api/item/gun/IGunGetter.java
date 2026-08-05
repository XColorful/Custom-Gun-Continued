/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.core.api.item.gun;

import dev.xcolorful.customgun.core.api.item.IGun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface IGunGetter {

    static @Nullable IGun fromItemStack(@Nullable ItemStack gunItem) {
        if (gunItem == null) return null;
        return gunItem.getItem() instanceof IGun iGun ? iGun : null;
    }
    static @Nullable IGun fromMainHand(@Nullable LivingEntity livingEntity) {
        if (livingEntity == null) return null;
        return livingEntity.getMainHandItem().getItem() instanceof IGun iGun ? iGun : null;
    }

    // --------Deprecated--------

    @Deprecated static @Nullable IGun getIGunOrNull(@Nullable ItemStack gunItem) {
        return fromItemStack(gunItem);
    }
    @Deprecated static boolean mainHandHoldGun(LivingEntity livingEntity) {
        return fromMainHand(livingEntity) != null;
    }
}
