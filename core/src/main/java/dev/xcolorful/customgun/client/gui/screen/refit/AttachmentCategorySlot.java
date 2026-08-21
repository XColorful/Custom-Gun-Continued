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
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AttachmentCategorySlot extends Button implements IStackTooltip {

    private final AttachmentCategory category;
    private final Inventory inventory;
    private final int gunItemIndex;
    private final boolean isSelected;
    private final boolean hasAttachmentLock;
    private final boolean isCategoryEnabled;

    // --------Cache--------
    private ItemStack attachmentItem = ItemStack.EMPTY;

    public AttachmentCategorySlot(int pX, int pY, AttachmentCategory category, int gunItemIndex, Inventory inventory,
                                  boolean isSelected, boolean hasAttachmentLock, boolean isCategoryEnabled, Button.OnPress onPress) {
        super(pX, pY, CustomTexture.SLOT.getWidth(), CustomTexture.SLOT.getHeight(), Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.category = category;
        this.inventory = inventory;
        this.gunItemIndex = gunItemIndex;
        this.isSelected = isSelected;
        this.hasAttachmentLock = hasAttachmentLock;
        this.isCategoryEnabled = isCategoryEnabled;
        this.attachmentItem = this.getAttachmentItem();
    }

    public static int getSlotTextureXOffset(AttachmentCategory attachmentCategory) {
        return CustomTexture.ATTACHMENT_CATEGORIES.getHeight() * attachmentCategory.ordinal();
    }

    public AttachmentCategory getCategory() {
        return this.category;
    }

    public ItemStack getAttachmentItem() {
        ItemStack gunItem = this.inventory.getItem(this.gunItemIndex);
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return ItemStack.EMPTY;

        return iGun.getAttachment(gunItem, this.category);
    }

    public boolean hasAttachmentLock() {
        return this.hasAttachmentLock;
    }
    public boolean isCategoryEnabled() {
        return this.isCategoryEnabled;
    }

    // --------AbstractButton--------

    public void _renderContent(@NotNull GuiGraphics graphics,
                               int pMouseX, int pMouseY,
                               float pPartialTick) {
        // 悬浮显示文本
        if (this.isHoveredOrFocused()) {
            Font font = Minecraft.getInstance().font;
            int xOffset = this.getX() + this.getWidth() / 2;
            int yOffset = this.getY() + 20;
            if (this.isSelected && !this.attachmentItem.isEmpty()) {
                yOffset = this.getY() + 30;
            }
            ClientGuiUtils.Graphics.drawCenteredText(graphics,
                    font,
                    category.getCategoryLang().copy(),
                    xOffset, yOffset,
                    Color64._FFFFFF.getRGB());
        }

        {
            ItemStack gunItem = this.inventory.getItem(this.gunItemIndex);
            @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
            if (iGun == null) return;
        }

        ClientRenderHelper.GL._disableDepthTest();
        ClientRenderHelper.GL._enableBlend(); {
            { // 渲染外框
                CustomTexture texture = this.isSelected || this.isHoveredOrFocused() ? CustomTexture.SLOT_SELECTED : CustomTexture.SLOT;

                int startX = this.getX();
                int startY = this.getY();
                int width = this.width;
                int height = this.height;

                int uOffset = 0;
                int vOffset = 0;
                int uWidth = texture.getWidth();
                int vHeight = texture.getHeight();

                ClientGuiUtils.blitGuiTexture(graphics,
                        texture,
                        startX, startY,
                        width, height,
                        uOffset, vOffset,
                        uWidth, vHeight);
            }

            { // 渲染内部物品，或者空置时的icon
                int startX = this.getX();
                int startY = this.getY();

                if (!this.attachmentItem.isEmpty()) {
                    // 配件物品
                    startX += 1;
                    startY += 1;

                    ClientGuiUtils.Graphics.drawItem(graphics,
                            this.attachmentItem,
                            startX, startY);
                } else if (this.isCategoryEnabled) {
                    // 配件类型
                    CustomTexture texture = CustomTexture.ATTACHMENT_CATEGORIES;

                    int width = this.width;
                    int height = this.height;

                    int uOffset = getSlotTextureXOffset(category);
                    int vOffset = 0;
                    int uWidth = texture.getHeight(); // 在横向长方形里取正方形
                    int vHeight = texture.getHeight();

                    ClientGuiUtils.blitGuiTexture(graphics,
                            texture,
                            startX, startY,
                            width, height,
                            uOffset, vOffset,
                            uWidth, vHeight);
                }
            }

            if (this.isCategoryEnabled && this.hasAttachmentLock) {
                // 配件锁
                CustomTexture texture = CustomTexture.ATTACHMENT_CATEGORIES;

                int startX = this.getX();
                int startY = this.getY();
                int width = this.width;
                int height = this.height;

                int uOffset = getSlotTextureXOffset(AttachmentCategory.NONE);
                int vOffset = 0;
                int uWidth = texture.getHeight(); // 在横向长方形里取正方形
                int vHeight = texture.getHeight();

                ClientGuiUtils.blitGuiTexture(graphics,
                        texture,
                        startX, startY,
                        width, height,
                        uOffset, vOffset,
                        uWidth, vHeight);
            }
        }
        ClientRenderHelper.GL._enableDepthTest();
        ClientRenderHelper.GL._disableBlend();
    }

    // --------IStackTooltip--------

    @Override
    public void renderTooltip(Consumer<ItemStack> consumer) {
        if (!this.isHoveredOrFocused()) return;

        if (this.attachmentItem.isEmpty()) return;

        consumer.accept(this.attachmentItem);
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
