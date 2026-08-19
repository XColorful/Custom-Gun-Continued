/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.screen.refit;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.xcolorful.customgun.client.api.minecraft.texture.CustomTexture;
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
    private boolean selected = false;
    private ItemStack attachmentItem = ItemStack.EMPTY;

    public AttachmentCategorySlot(int pX, int pY, AttachmentCategory category, int gunItemIndex, Inventory inventory, Button.OnPress onPress) {
        super(pX, pY, CustomTexture.SLOT.getWidth(), CustomTexture.SLOT.getHeight(), Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.category = category;
        this.inventory = inventory;
        this.gunItemIndex = gunItemIndex;
    }

    public static int getSlotTextureXOffset(IGun iGun, ItemStack gunItem, AttachmentCategory attachmentCategory) {
        if (!iGun.isAttachmentEnabled(gunItem, attachmentCategory)) {
            attachmentCategory = AttachmentCategory.NONE;
        }

        return CustomTexture.ATTACHMENT_CATEGORIES.getHeight() * attachmentCategory.ordinal();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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

    public boolean isAttachmentEnabled() {
        ItemStack gunItem = this.inventory.getItem(this.gunItemIndex);
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return false;

        return iGun.isAttachmentEnabled(gunItem, this.category);
    }

    // --------AbstractButton--------

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        if (this.isHoveredOrFocused()) {
            Font font = Minecraft.getInstance().font;
            int yOffset = this.getY() + 20;
            if (this.selected && !this.attachmentItem.isEmpty()) {
                yOffset = this.getY() + 30;
            }
            graphics.drawCenteredString(font, category.getCategoryLang().copy(), this.getX() + this.getWidth() / 2, yOffset, Color64._FFFFFF.getRGB());
        }

        ItemStack gunItem = this.inventory.getItem(this.gunItemIndex);
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend(); {
            { // 渲染外框
                CustomTexture texture = CustomTexture.SLOT;

                int startX = this.getX();
                int startY = this.getY();
                int endX = this.width;
                int endY = this.height;

                int uOffset = 0;
                int vOffset = 0;
                int uWidth = texture.getWidth();
                int vHeight = texture.getHeight();

                if (!this.isHoveredOrFocused() && !this.selected) {
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
            }

            { // 渲染内部物品，或者空置时的icon
                int startX = this.getX();
                int startY = this.getY();

                this.attachmentItem = iGun.getAttachment(gunItem, category);
                if (!attachmentItem.isEmpty()) {
                    // 配件物品
                    graphics.renderItem(attachmentItem, startX + 1, startY + 1);
                } else {
                    // 配件类型
                    CustomTexture texture = CustomTexture.ATTACHMENT_CATEGORIES;

                    startX += 2;
                    startY += 2;
                    int endX = this.width - 4;
                    int endY = this.height - 4;

                    int uOffset = getSlotTextureXOffset(iGun, gunItem, category);
                    int vOffset = 0;
                    int uWidth = texture.getHeight(); // 在横向长方形里取正方形
                    int vHeight = texture.getHeight();

                    graphics.blit(texture.location,
                            startX, startY,
                            endX, endY,
                            uOffset, vOffset,
                            uWidth, vHeight,
                            texture.getWidth(), texture.getHeight());
                }
            }
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    // --------IStackTooltip--------

    @Override
    public void renderTooltip(Consumer<ItemStack> consumer) {
        if (!this.isHoveredOrFocused()) return;

        if (this.attachmentItem.isEmpty()) return;

        consumer.accept(this.attachmentItem);
    }
}
