/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.forge.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;

public class ForgeEvent implements IEvent {

    protected Event event;

    public ForgeEvent(Event event) {
        this.event = event;
    }
    @Override public EventType getType() {
        return null;
    }
    @Override public boolean isCancelable() {
        return event.isCancelable();
    }

    public boolean isCanceled() {
        return this.event.isCanceled();
    }

    public void setCanceled(boolean cancel) {
        if (this.isCancelable())
            this.event.setCanceled(cancel);
    }

    public Object getEvent() {
        return this.event;
    }

    @Override
    public @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source) {
        return null;
    }

    @Override public String getTextName() {
        return null;
    }
    @Override public Component getDisplayName() {
        return null;
    }
}
