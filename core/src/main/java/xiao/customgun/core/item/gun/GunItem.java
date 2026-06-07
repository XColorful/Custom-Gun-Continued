/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.core.item.gun;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import xiao.customgun.client.item.gun._GunItem;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.item.gun.GunDataAccessor;
import xiao.customgun.core.gui.tooltip.GunTooltip;
import xiao.customgun.core.init.registry.ModItems;

import java.util.Optional;

public class GunItem extends Item implements IGun, GunDataAccessor {

    protected GunItem(Properties properties) {
        super(properties);
    }
    public GunItem() {
        this(ModItems.CUSTOM_ITEM_PROPERTY);
    }

    // --------Item--------

    /**
     * 阻止玩家手臂挥动
     */
    @Override
    public boolean onEntitySwing(ItemStack gunItem, LivingEntity livingShooter) {
        return true;
    }

    /**
     * 获取在 Tooltip 中渲染的图片
     */
    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack gunItem) {
        return Optional.ofNullable(GunTooltip.fromItem(gunItem));
    }

    // --------IGunShootManager--------

    // --------Client--------

    @Override
    public @NotNull Component getName(@NotNull ItemStack gunItem) {
        var name = _GunItem.getName(this, gunItem);
        return name != null ? name : super.getName(gunItem);
    }
}
