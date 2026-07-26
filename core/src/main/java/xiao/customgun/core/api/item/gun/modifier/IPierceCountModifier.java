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
import xiao.customgun.core.api.entity.shooter.modifier.ShooterGunModifierCache;
import xiao.customgun.core.resource.data.data.attachment._SimpleModifierData;

public interface IPierceCountModifier<T extends ResourcePojo<T>> extends IGunModifier<T, _SimpleModifierData, Integer> {

    @Override
    default @Nullable Integer getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                                      @NotNull GunData gunData) {
        return gunData.getBulletData().getPierceCount();
    }

    static @Nullable Integer getValue(ShooterGunModifierCache cache, IGunModifierHolder modifierType) {
        return cache.getValue(modifierType, IPierceCountModifier.class);
    }
    static void setValue(ShooterGunModifierCache cache, IGunModifierHolder modifierHolder, Integer value) {
        cache.setValue(modifierHolder, IPierceCountModifier.class, value);
    }
}
