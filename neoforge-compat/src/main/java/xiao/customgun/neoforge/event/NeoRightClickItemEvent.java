/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforge.event;

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
import xiao.customgun.core.api.common.McLogicalSide;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IRightClickItemEvent;
import xiao.customgun.core.api.minecraft.CommandLevel;
import xiao.customgun.core.util.Vec3Utils;
import xiao.customgun.neoforge.common.McSideHelper;

public class NeoRightClickItemEvent extends NeoEvent implements IRightClickItemEvent {

    protected PlayerInteractEvent.RightClickItem rightClickItemEvent;

    public NeoRightClickItemEvent(Event event) {
        super(event);
        if (event instanceof PlayerInteractEvent.RightClickItem eventIn) {
            this.rightClickItemEvent = eventIn;
        } else {
            throw new RuntimeException("Expected RightClickItem but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.RIGHT_CLICK_ITEM_EVENT;
    }

    @Override
    public McLogicalSide getLogicalSide() {
        return McSideHelper.convert(rightClickItemEvent.getSide());
    }

    @Override
    public Player getEntity() {
        return rightClickItemEvent.getEntity();
    }

    @Override
    public InteractionHand getHand() {
        return rightClickItemEvent.getHand();
    }

    @Override
    public ItemStack getItemStack() {
        return rightClickItemEvent.getItemStack();
    }

    @Override
    public BlockPos getBlockPos() {
        return rightClickItemEvent.getPos();
    }

    @Override
    public @Nullable Direction getFace() {
        return rightClickItemEvent.getFace();
    }

    @Override
    public Level getLevel() {
        return rightClickItemEvent.getLevel();
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
        return "NeoRightClickItemEvent";
    }

    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
