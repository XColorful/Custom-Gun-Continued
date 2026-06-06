/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.gun;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.gui.tooltip.GunTooltip;

import java.util.Optional;

public abstract class AbstractGunItem extends Item implements IGun {

    protected AbstractGunItem(Properties properties) {
        super(properties);
    }

    /**
     * 获取在 Tooltip 中渲染的图片
     */
    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack gunItem) {
        return Optional.ofNullable(GunTooltip.fromItem(gunItem));
    }
}
