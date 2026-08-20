/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.screen.refit;

import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import dev.xcolorful.customgun.client.util.ClientGuiUtils;
import dev.xcolorful.customgun.client.util.ClientRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class RefitTurnPageButton extends Button {

    public static final int xMargin = (CustomTexture.SLOT.getWidth() - CustomTexture.TURN_PAGE.getWidth() / 2) / 2;
    public static final int yMargin = (CustomTexture.SLOT.getHeight() - CustomTexture.TURN_PAGE.getHeight() / 2) / 2;

    private final boolean isPreviousPage;

    public RefitTurnPageButton(int pX, int pY, boolean isPreviousPage, OnPress pOnPress) {
        super(pX + xMargin, pY + yMargin, CustomTexture.TURN_PAGE.getWidth() / 2, CustomTexture.TURN_PAGE.getHeight() / 2, Component.empty(), pOnPress, DEFAULT_NARRATION);
        this.isPreviousPage = isPreviousPage;
    }

    // --------AbstractButton--------

    public void _renderContent(@NotNull GuiGraphics graphics,
                               int pMouseX, int pMouseY,
                               float pPartialTick) {
        ClientRenderHelper.GL._disableDepthTest();
        ClientRenderHelper.GL._enableBlend(); {
            CustomTexture texture = CustomTexture.TURN_PAGE;

            int startX = getX();
            int startY = getY();
            int endX = this.width;
            int endY = this.height;

            int uOffset = this.isHoveredOrFocused() ? texture.getWidth() / 2 : 0;
            int vOffset = this.isPreviousPage ? 0 : texture.getHeight() / 2;
            int uWidth = texture.getWidth() / 2;
            int vHeight = texture.getHeight() / 2;

            ClientGuiUtils.blitGuiTexture(graphics,
                    texture,
                    startX, startY,
                    endX, endY,
                    uOffset, vOffset,
                    uWidth, vHeight);
        }
        ClientRenderHelper.GL._enableDepthTest();
        ClientRenderHelper.GL._disableBlend();
    }

    // --------Compat--------
    // 跨版本适配层
    // 其他类可直接Ctrl CV

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics,
                             int pMouseX, int pMouseY,
                             float pPartialTick) {
        this._renderContent(graphics, pMouseX, pMouseY, pPartialTick);
    }
}
