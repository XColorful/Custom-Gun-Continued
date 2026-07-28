/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.api.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.gun.IGunRuntime;
import xiao.customgun.core.api.item.gun.IGunDataAccess;
import xiao.customgun.core.api.item.gun.IGunGetter;

public interface IGun extends IGunRuntime, IAnimationItem,
        IGunDataAccess, IGunGetter,
        IPojoItem {

    @Override
    default @NotNull Identifier getPojoLocation(ItemStack gunItem) {
        return this.getGunLocation(gunItem);
    }
    @Override
    default void setPojoLocation(ItemStack gunItem, Identifier gunLocation) {
        this.setGunLocation(gunItem, gunLocation);
    }
}
