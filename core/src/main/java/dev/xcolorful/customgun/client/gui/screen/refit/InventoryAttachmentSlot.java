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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class InventoryAttachmentSlot extends Button implements IStackTooltip {

    private final int slotIndex;
    private final Inventory inventory;

    public InventoryAttachmentSlot(int pX, int pY, int slotIndex, Inventory inventory, OnPress onPress) {
        super(pX, pY, 18, 18, Component.empty(), onPress, DEFAULT_NARRATION);
        this.slotIndex = slotIndex;
        this.inventory = inventory;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    // --------AbstractButton--------

    @Override
    public void renderWidget(@Nonnull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend(); {
            CustomTexture texture = CustomTexture.SLOT;

            int startX = getX();
            int startY = getY();
            int endX = this.width;
            int endY = this.height;

            int uOffset = 0;
            int vOffset = 0;
            int uWidth = texture.getWidth();
            int vHeight = texture.getHeight();

            if (!this.isHoveredOrFocused()) {
                startX += 1;
                startY += 1;
                endX -= 2;
                endY -= 2;

                uOffset += 1;
                vOffset += 1;
                uWidth -= 2;
                vHeight -= 2;
            }

            graphics.blit(texture.getLocation(),
                    startX, startY,
                    endX, endY,
                    uOffset, vOffset,
                    uWidth, vHeight,
                    texture.getWidth(), texture.getHeight());

            graphics.renderItem(inventory.getItem(slotIndex), getX() + 1, getY() + 1);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // --------IStackTooltip--------

    @Override
    public void renderTooltip(Consumer<ItemStack> consumer) {
        // 没聚焦或选中
        if (!this.isHoveredOrFocused()) return;

        if (this.slotIndex < 0 || this.slotIndex >= this.inventory.getContainerSize()) {
            return;
        }
        ItemStack item = this.inventory.getItem(slotIndex);

        consumer.accept(item);
    }
}
