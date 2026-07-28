/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.neoforge.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.ILivingAttackEvent;
import xiao.customgun.core.api.minecraft.CommandLevel;

public class NeoLivingAttackEvent extends NeoEvent implements ILivingAttackEvent {

    protected LivingAttackEvent livingAttackEvent;

    public NeoLivingAttackEvent(Event event) {
        super(event);
        if (event instanceof LivingAttackEvent eventIn) {
            this.livingAttackEvent = eventIn;
        } else {
            throw new RuntimeException("Expected LivingAttackEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.LIVING_ATTACK_EVENT;
    }

    @Override
    public @NotNull LivingEntity getEntity() {
        return livingAttackEvent.getEntity();
    }

    @Override
    public @NotNull DamageSource getSource() {
        return livingAttackEvent.getSource();
    }

    @Override
    public float getDamageAmount() {
        return livingAttackEvent.getAmount();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        @NotNull LivingEntity entity = this.getEntity();
        Level level = entity.level();
        if (level != null && level.isClientSide()) return null;
        return new CommandSourceStack(
                source != null ? source : CommandSource.NULL,
                entity.position(),
                entity.getRotationVector(),
                (ServerLevel) level,
                CommandLevel.permission(4),
                this.getTextName(),
                this.getDisplayName(),
                level.getServer(),
                entity
        );
    }

    @Override public String getTextName() {
        return this.getEntity().getName().getString();
    }
    @Override public Component getDisplayName() {
        return this.getEntity().getDisplayName();
    }
}
