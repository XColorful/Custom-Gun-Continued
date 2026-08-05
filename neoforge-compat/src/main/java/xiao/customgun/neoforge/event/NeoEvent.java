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
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.core.api.event.EventType;
import xiao.customgun.core.api.event.IEvent;

public class NeoEvent implements IEvent {

    protected Event event;

    public NeoEvent(Event event) {
        this.event = event;
    }
    @Override public EventType getType() {
        return null;
    }
    @Override public boolean isCancelable() {
        return this.event instanceof ICancellableEvent;
    }

    @Override
    public boolean isCanceled() {
        if (this.event instanceof ICancellableEvent cancellableEvent) {
            return cancellableEvent.isCanceled();
        }
        return false;
    }

    @Override
    public void setCanceled(boolean cancel) {
        if (this.event instanceof ICancellableEvent cancellableEvent) {
            cancellableEvent.setCanceled(cancel);
        }
    }

    @Override
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