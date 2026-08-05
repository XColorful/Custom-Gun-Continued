/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.forge.event;

import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IRightClickBlockEvent;
import dev.xcolorful.customgun.core.api.minecraft.CommandLevel;
import dev.xcolorful.customgun.core.api.minecraft.TriResult;
import dev.xcolorful.customgun.core.util.Vec3Utils;
import dev.xcolorful.customgun.forge.common.McSideHelper;
import dev.xcolorful.customgun.forge.minecraft.TriResultHelper;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public class ForgeRightClickBlockEvent extends ForgeEvent implements IRightClickBlockEvent {

    protected PlayerInteractEvent.RightClickBlock rightClickBlockEvent;

    public ForgeRightClickBlockEvent(Event event) {
        super(event);
        if (event instanceof PlayerInteractEvent.RightClickBlock eventIn) {
            this.rightClickBlockEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RightClickBlock but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.RIGHT_CLICK_BLOCK_EVENT;
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return McSideHelper.convert(rightClickBlockEvent.getSide());
    }

    @Override
    public Player getEntity() {
        return rightClickBlockEvent.getEntity();
    }

    @Override
    public InteractionHand getHand() {
        return rightClickBlockEvent.getHand();
    }

    @Override
    public ItemStack getItemStack() {
        return rightClickBlockEvent.getItemStack();
    }

    @Override
    public BlockPos getBlockPos() {
        return rightClickBlockEvent.getPos();
    }

    @Override
    public @Nullable Direction getFace() {
        return rightClickBlockEvent.getFace();
    }

    @Override
    public Level getLevel() {
        return rightClickBlockEvent.getLevel();
    }

    @Override
    public TriResult getUseBlock() {
        return TriResultHelper.convert(rightClickBlockEvent.getUseBlock());
    }

    @Override
    public TriResult getUseItem() {
        return TriResultHelper.convert(rightClickBlockEvent.getUseItem());
    }

    @Override
    public BlockHitResult getHitVec() {
        return rightClickBlockEvent.getHitVec();
    }

    @Override
    public void setUseBlock(TriResult triggerBlock) {
        rightClickBlockEvent.setUseBlock(TriResultHelper.convert(triggerBlock));
    }

    @Override
    public void setUseItem(TriResult triggerItem) {
        rightClickBlockEvent.setUseItem(TriResultHelper.convert(triggerItem));
    }

    @Override
    public InteractionResult getCancellationResult() {
        return rightClickBlockEvent.getCancellationResult();
    }

    @Override
    public void setCancellationResult(InteractionResult result) {
        rightClickBlockEvent.setCancellationResult(result);
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
//        if (this.getLogicalSide().isClient()) return null;
        if (getHand() != InteractionHand.MAIN_HAND) return null; // 只给 function 传主手触发的事件
        Level level = getLevel();
        if (level != null && level.isClientSide()) return null;
        Player player = this.getEntity();
        return new CommandSourceStack(
                source != null ? source : CommandSource.NULL,
                Vec3Utils.getCenter(this.getBlockPos()),
                player.getRotationVector(),
                (ServerLevel) level,
                CommandLevel.permission(4),
                this.getTextName(),
                this.getDisplayName(),
                level.getServer(),
                player
        );
    }

    @Override public String getTextName() {
        return "ForgeRightClickBlockEvent";
    }

    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}