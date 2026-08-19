/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.screen.refit;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class RefitTurnPageButton extends Button {

    private final boolean isPreviousPage;

    public RefitTurnPageButton(int pX, int pY, boolean isPreviousPage, OnPress pOnPress) {
        super(pX, pY, CustomTexture.SLOT.getWidth(), 8, Component.empty(), pOnPress, DEFAULT_NARRATION);
        this.isPreviousPage = isPreviousPage;
    }

    // --------AbstractButton--------

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend(); {
            CustomTexture texture = CustomTexture.TURN_PAGE;

            int startX = getX();
            int startY = getY();
            int endX = this.width;
            int endY = this.height;

            int uOffset = 0;
            int vOffset = this.isPreviousPage ? 0 : texture.getHeight() / 2;
            int uWidth = texture.getWidth();
            int vHeight = texture.getHeight() / 2;

            if (!this.isHoveredOrFocused()) {
                // 没聚焦就缩小一圈
                startX += 1;
                startY += 1;
                endX -= 2;
                endY -= 2;

                uOffset += 10;
                vOffset += 10;
                uWidth -= 20;
                vHeight -= 20;
            }

            graphics.blit(texture.getLocation(),
                    startX, startY,
                    endX, endY,
                    uOffset, vOffset,
                    uWidth, vHeight,
                    texture.getWidth(), texture.getHeight());
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}
