/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforge.event;

import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.core.api.event.IServerTickEvent;
import dev.xcolorful.customgun.core.api.minecraft.CommandLevel;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NeoServerTickEvent extends NeoEvent implements IServerTickEvent {

    protected TickEvent.ServerTickEvent serverTickEvent;

    public NeoServerTickEvent(Event event) {
        super(event);
        if (event instanceof TickEvent.ServerTickEvent eventIn) {
            this.serverTickEvent = eventIn;
        } else {
            throw new RuntimeException("Expected ServerTickEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.SERVER_TICK_EVENT;
    }

    @Override
    public MinecraftServer getServer() {
        return serverTickEvent.getServer();
    }

    @Override
    public @NotNull CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        MinecraftServer server = this.getServer();
        return new CommandSourceStack(
                source != null ? source : CommandSource.NULL,
                Vec3.ZERO,
                Vec2.ZERO,
                server.overworld(),
                CommandLevel.permission(4),
                this.getTextName(),
                this.getDisplayName(),
                server,
                null
        );
    }

    @Override public String getTextName() {
        return "NeoServerTickEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}