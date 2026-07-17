/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.entity.gun;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.IGunGetter;

public class GunPropertyManager {

    public static void postChangeEvent(LivingEntity livingShooter) {
        postChangeEvent(livingShooter, livingShooter.getMainHandItem());
    }
    public static void postChangeEvent(LivingEntity livingShooter, ItemStack gunItem) {
        IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        var gunLocation = iGun.getGunLocation(gunItem);
        // TODO
    }
}
