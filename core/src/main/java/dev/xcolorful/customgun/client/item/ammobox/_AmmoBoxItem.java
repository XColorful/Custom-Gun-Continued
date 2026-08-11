/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.item.ammobox;

import dev.xcolorful.customgun.core.api.item.IAmmoBox;
import dev.xcolorful.customgun.core.api.item.ammobox.IAmmoBoxGetter;
import dev.xcolorful.customgun.core.developer.PlannedRefactor;
import dev.xcolorful.customgun.core.util.NBTUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

public class _AmmoBoxItem {

    public static int getColor(ItemStack ammoItem, int tintIndex) {
        // 只有基础类型染色
        if (tintIndex > 0) return -1;

        return getDisplayComponent(ammoItem);
    }
    /**
     * 获取 DataComponents.DYED_COLOR
     * <p>
     * 原版逻辑不使用 {@link NBTUtils} 跨版本封装
     */
    private static int getDisplayComponent(ItemStack ammoItem) {
        return DyedItemColor.getOrDefault(ammoItem, PlannedRefactor.MAGIC_AMMO_BOX_COLOR);
    }

    public static float getStatus(ItemStack ammoItem, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        @Nullable IAmmoBox iAmmoBox = IAmmoBoxGetter.fromItemStack(ammoItem);
        if (iAmmoBox == null) return -1;
        return iAmmoBox.getStatusMask(ammoItem);
    }
}
