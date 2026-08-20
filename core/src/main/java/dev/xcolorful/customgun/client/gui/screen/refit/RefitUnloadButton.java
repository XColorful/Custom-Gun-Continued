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

/**
 * 图标长宽各为0.5倍
 */
public class RefitUnloadButton extends Button {

    public static final int unloadButtonXMargin = (CustomTexture.SLOT.getWidth() - CustomTexture.UNLOAD.getWidth() / 2 / 2) / 2;
    public static final int unloadButtonYMargin = 2;

    public RefitUnloadButton(int pX, int pY, Button.OnPress pOnPress) {
        super(pX + unloadButtonXMargin, pY + unloadButtonYMargin, CustomTexture.UNLOAD.getHeight() / 2, CustomTexture.UNLOAD.getHeight() / 2, Component.empty(), pOnPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend(); {
            CustomTexture texture = CustomTexture.UNLOAD;

            int startX = getX();
            int startY = getY();
            int endX = this.width;
            int endY = this.height;

            int uOffset = this.isHoveredOrFocused() ? texture.getWidth() / 2 : 0;
            int vOffset = 0;
            int uWidth = texture.getWidth() / 2;
            int vHeight = texture.getHeight();

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
