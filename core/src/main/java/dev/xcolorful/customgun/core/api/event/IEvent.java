/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.core.api.event;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface IEvent {

    EventType getType();

    default boolean isCancelable() {
        return true;
    }

    boolean isCanceled();

    void setCanceled(boolean cancel);

    default Object getEvent() {
        return this;
    }

    @Nullable CommandSourceStack createCommandSourceStack(@Nullable CommandSource source);
    String getTextName();
    Component getDisplayName();
}
