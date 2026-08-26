/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package dev.xcolorful.customgun.client.api.event;

import dev.xcolorful.customgun.core.api.event.IEvent;
import net.minecraft.client.gui.GuiGraphics;

public interface IRenderGuiEvent extends IEvent {

    /**
     * @since 26.1 {@code GuiGraphicsExtractor}
     */
    GuiGraphics getGuiGraphics();

    /**
     * @return {@code RenderGuiEvent.getPartialTick().getGameTimeDeltaPartialTick(true)}
     */
    float getPartialTick();
}
