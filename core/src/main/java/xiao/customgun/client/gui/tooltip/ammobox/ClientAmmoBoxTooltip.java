/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.gui.tooltip.ammobox;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xiao.customgun.client.api.gui.tooltip.BaseTooltipContext;
import xiao.customgun.client.api.gui.tooltip.BaseTooltipView;
import xiao.customgun.core.gui.tooltip.ammobox.AmmoBoxTooltip;

public class ClientAmmoBoxTooltip implements ClientTooltipComponent {

    public final View view;
    private final Context context;

    public ClientAmmoBoxTooltip(AmmoBoxTooltip ammoBoxTooltip) {
        this.view = new View();
        this.context = new Context(this.view, ammoBoxTooltip);
    }

    // --------ClientTooltipComponent--------

    @Override public int getHeight(Font font) {
        return this.context.getHeight();
    }
    @Override public int getWidth(Font font) {
        return this.context.getMaxWidth();
    }
    @Override
    public void renderText(GuiGraphics guiGraphics,
                           Font font, int pX, int pY) {
    }
    @Override
    public void renderImage(Font font, int pX, int pY,
                            int width, int height,
                            GuiGraphics guiGraphics) {
    }

    // --------record--------

    @ApiStatus.Internal
    public static final class View extends BaseTooltipView {
        public @Nullable Component ammoName;
        public @Nullable Component ammoCount;
        public View() {
        }
    }
    @ApiStatus.Internal
    public static final class Context extends BaseTooltipContext<View> {
        public final AmmoBoxTooltip ammoBoxTooltip;
        public Context(@NotNull View view, AmmoBoxTooltip ammoBoxTooltip) {
            super(view);
            this.ammoBoxTooltip = ammoBoxTooltip;
            this.buildView();
        }
        @Override
        protected void buildView() {
        }
        @Override
        protected int calculateHeight() {
            return 0;
        }
    }
}
