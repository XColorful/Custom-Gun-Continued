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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class InventoryAttachmentSlot extends Button implements IStackTooltip {

    private final int slotIndex;
    private final Inventory inventory;

    public InventoryAttachmentSlot(int pX, int pY, int slotIndex, Inventory inventory, OnPress onPress) {
        super(pX, pY, CustomTexture.SLOT.getWidth(), CustomTexture.SLOT.getHeight(), Component.empty(), onPress, DEFAULT_NARRATION);
        this.slotIndex = slotIndex;
        this.inventory = inventory;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    // --------AbstractButton--------

    public void _renderContent(@NotNull GuiGraphicsExtractor graphics,
                               int pMouseX, int pMouseY,
                               float pPartialTick) {
        ClientRenderHelper.GL._disableDepthTest();
        ClientRenderHelper.GL._enableBlend(); {
            { // 渲染外框
                CustomTexture texture = this.isHoveredOrFocused() ? CustomTexture.SLOT_SELECTED : CustomTexture.SLOT;

                int startX = this.getX();
                int startY = this.getY();
                int endX = this.width;
                int endY = this.height;

                int uOffset = 0;
                int vOffset = 0;
                int uWidth = texture.getWidth();
                int vHeight = texture.getHeight();

                ClientGuiUtils.blitGuiTexture(graphics,
                        texture,
                        startX, startY,
                        endX, endY,
                        uOffset, vOffset,
                        uWidth, vHeight);
            }

            { // 渲染物品
                int startX = this.getX() + 1;
                int startY = this.getY() + 1;

                graphics.item(inventory.getItem(slotIndex),
                        startX, startY);
            }
        }
        ClientRenderHelper.GL._enableDepthTest();
        ClientRenderHelper.GL._disableBlend();
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

    // --------Compat--------
    // 跨版本适配层
    // 其他类可直接Ctrl CV

    @Override
    public void extractContents(@NotNull GuiGraphicsExtractor graphics,
                             int pMouseX, int pMouseY,
                             float pPartialTick) {
        this._renderContent(graphics, pMouseX, pMouseY, pPartialTick);
    }
}
