/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.neoforgeclient.event;

import dev.xcolorful.customgun.client.api.event.IRenderGuiEvent;
import dev.xcolorful.customgun.core.api.event.EventType;
import dev.xcolorful.customgun.neoforge.event.NeoEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.jetbrains.annotations.Nullable;

public class NeoRenderGuiEvent extends NeoEvent implements IRenderGuiEvent {

    private final RenderGuiEvent.Post event;

    public NeoRenderGuiEvent(Event event) {
        super(event);
        if (event instanceof RenderGuiEvent.Post eventIn) {
            this.event = eventIn;
        } else {
            throw new RuntimeException("Expected RenderGuiEvent but received: " + event.getClass().getName());
        }
    }
    @Override public EventType getType() {
        return EventType.RENDER_GUI_EVENT;
    }

    @Override
    public GuiGraphics getGuiGraphics() {
        return this.event.getGuiGraphics();
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return "NeoRenderGuiEvent";
    }
    @Override public Component getDisplayName() {
        return Component.literal(getTextName());
    }
}
