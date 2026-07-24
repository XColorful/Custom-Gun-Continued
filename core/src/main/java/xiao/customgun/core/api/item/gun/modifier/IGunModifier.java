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
import xiao.customgun.core.api.item.IItemModifier;
import xiao.customgun.core.resource.ResourcePojo;
import xiao.customgun.core.resource.data.data.GunData;

/**
 * 枪械修饰工具
 */
public interface IGunModifier<T extends ResourcePojo<T>, K, V> extends IItemModifier<T, K, V> {

    @Nullable V getBase(@NotNull IGun iGun, @NotNull ItemStack gunItem,
                        @NotNull GunData gunData);
}
