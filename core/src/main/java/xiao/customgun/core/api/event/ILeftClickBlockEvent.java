/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.core.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.common.ILogicalSideOnly;
import xiao.customgun.core.api.minecraft.HandAction;
import xiao.customgun.core.api.minecraft.TriResult;

public interface ILeftClickBlockEvent extends IEvent, ILogicalSideOnly {

    Player getEntity();
    InteractionHand getHand();
    ItemStack getItemStack();
    BlockPos getBlockPos();
    @Nullable Direction getFace();
    Level getLevel();

    TriResult getUseBlock();
    TriResult getUseItem();
    HandAction getAction();
    void setUseBlock(TriResult triggerBlock);
    void setUseItem(TriResult triggerItem);
}
