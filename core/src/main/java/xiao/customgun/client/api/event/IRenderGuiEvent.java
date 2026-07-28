/*
 * Copyright (c) 2025-2026 XiaoColorful (https://github.com/XColorful)
 * SPDX-License-Identifier: GPL-3.0-or-later
 *
 * Source: https://github.com/XColorful/BattleRoyale
 */

package xiao.customgun.client.api.event;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface IRenderGuiEvent {

    GuiGraphicsExtractor getGuiGraphics();
}
