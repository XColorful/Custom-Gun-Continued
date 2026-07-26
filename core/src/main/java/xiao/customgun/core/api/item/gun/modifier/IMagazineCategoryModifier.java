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
import xiao.customgun.core.api.item.attachment.MagazineCategory;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.GunData;
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;

public interface IMagazineCategoryModifier<T extends ResourcePojo<T>> extends IGunModifier<T, MagazineCategory, MagazineCategory> {

    @Override
    default @Nullable MagazineCategory getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                                @NotNull GunData gunData) {
        return MagazineCategory.NONE;
    }

    static @Nullable MagazineCategory getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IMagazineCategoryModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, MagazineCategory value) {
        cache.setValue(modifierHolder, IMagazineCategoryModifier.class, value);
    }
}
