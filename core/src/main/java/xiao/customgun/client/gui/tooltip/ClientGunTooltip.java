/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.gui.tooltip;

import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import xiao.customgun.client.api.item.gun.GunTooltipMask;

import java.util.EnumSet;
import java.util.List;

public class ClientGunTooltip {

    private final DrawTooltipContext drawContext;

    public ClientGunTooltip() {
        this.drawContext = null;
    }

    public int getHeight() {
        return GunTooltipMask.calculateHeight(this.drawContext);
    }

    public record DrawTooltipContext(@Nullable List<FormattedCharSequence> desc, EnumSet<GunTooltipMask> visibleParts) {
    }
}
