/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.client.api.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.IEvent;

import java.util.List;

public interface IItemTooltipEvent extends IEvent {

    @Nullable Player getPlayer();

    @NotNull ItemStack getItemStack();

    @NotNull List<Component> getToolTip();

    @NotNull TooltipFlag getFlags();
}
