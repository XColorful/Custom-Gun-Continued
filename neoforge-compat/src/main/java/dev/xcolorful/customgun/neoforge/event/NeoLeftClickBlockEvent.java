/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforge.event;

import dev.xcolorful.customgun.core.api.common.McLogicalSide;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.ILeftClickBlockEvent;
import dev.xcolorful.customgun.core.api.minecraft.CommandLevel;
import dev.xcolorful.customgun.core.api.minecraft.HandAction;
import dev.xcolorful.customgun.core.api.minecraft.TriResult;
import dev.xcolorful.customgun.core.util.Vec3Utils;
import dev.xcolorful.customgun.neoforge.common.McSideHelper;
import dev.xcolorful.customgun.neoforge.minecraft.HandActionHelper;
import dev.xcolorful.customgun.neoforge.minecraft.TriResultHelper;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

public class NeoLeftClickBlockEvent extends NeoEvent implements ILeftClickBlockEvent {

    protected PlayerInteractEvent.LeftClickBlock leftClickBlockEvent;

    public NeoLeftClickBlockEvent(Event event) {
        super(event);
        if (event instanceof PlayerInteractEvent.LeftClickBlock eventIn) {
            this.leftClickBlockEvent = eventIn;
        } else {
            throw new RuntimeException("Expected LeftClickBlock but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.LEFT_CLICK_BLOCK_EVENT;
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return McSideHelper.convert(leftClickBlockEvent.getSide());
    }

    @Override
    public Player getEntity() {
        return leftClickBlockEvent.getEntity();
    }

    @Override
    public InteractionHand getHand() {
        return leftClickBlockEvent.getHand();
    }

    @Override
    public ItemStack getItemStack() {
        return leftClickBlockEvent.getItemStack();
    }

    @Override
    public BlockPos getBlockPos() {
        return leftClickBlockEvent.getPos();
    }

    @Override
    public @Nullable Direction getFace() {
        return leftClickBlockEvent.getFace();
    }

    @Override
    public Level getLevel() {
        return leftClickBlockEvent.getLevel();
    }

    @Override
    public TriResult getUseBlock() {
        return TriResultHelper.convert(leftClickBlockEvent.getUseBlock());
    }

    @Override
    public TriResult getUseItem() {
        return TriResultHelper.convert(leftClickBlockEvent.getUseItem());
    }

    @Override
    public HandAction getAction() {
        return HandActionHelper.convert(leftClickBlockEvent.getAction());
    }

    @Override
    public void setUseBlock(TriResult triggerBlock) {
        leftClickBlockEvent.setUseBlock(TriResultHelper.convert(triggerBlock));
    }

    @Override
    public void setUseItem(TriResult triggerItem) {
        leftClickBlockEvent.setUseItem(TriResultHelper.convert(triggerItem));
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
        return "NeoLeftClickBlockEvent";
    }

    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
