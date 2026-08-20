/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.screen.refit;

import dev.xcolorful.customgun.client.api.gui.screen.refit.IGunRefitScreen;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.renderer.model.BeamRender;
import dev.xcolorful.customgun.client.resource.assets.display.AttachmentDisplay;
import dev.xcolorful.customgun.client.resource.assets.display.GunDisplay;
import dev.xcolorful.customgun.client.resource.assets.display._LaserDisplay;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.client.resource.instance.data.ClientAttachmentIndexInstance;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentNBTAccessor;
import dev.xcolorful.customgun.core.api.item.attachment.IAttachmentGetter;
import dev.xcolorful.customgun.core.api.item.gun.IGunGetter;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class HSVSliderGroup {

    private boolean isDirty = false;

    private final Inventory inventory;
    private final int gunItemIndex;

    private final AttachmentCategory category;

    private final LaserColorSlider hueSlider;
    private final LaserColorSlider saturationSlider;

    public HSVSliderGroup(int screenWidth, int screenHeight, int width, int height, Inventory inventory, int gunItemIndex, @NotNull AttachmentCategory category) {
        int currentX = screenWidth - 140;
        int currentY = screenHeight - 64;
        this.inventory = inventory;
        this.gunItemIndex = gunItemIndex;
        this.category = category;

        int color = getColor(category);
        float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);

        hueSlider = new LaserColorSlider(currentX, currentY, width, height, Component.empty(), hsb[0], this);
        saturationSlider = new LaserColorSlider(currentX, currentY + 2 + height, width, height, Component.empty(), hsb[1], this);
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public LaserColorSlider getHueSlider() {
        return hueSlider;
    }

    public LaserColorSlider getSaturationSlider() {
        return saturationSlider;
    }

    /**
     * 往客户端写nbt是脏写，为了确保能实时预览染色效果
     * 在{@link IGunRefitScreen#resetScreen()}关闭界面的时候上传
     */
    public void apply() {
        ItemStack gunItem = inventory.getItem(gunItemIndex);
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);
        if (iGun == null) return;

        int rgb_new = Color.HSBtoRGB((float) hueSlider.getValue(), (float) saturationSlider.getValue(), 1f);

        this.isDirty = true;

        if (this.category == AttachmentCategory.NONE) {
            iGun.setLaserColorInt(gunItem, rgb_new);
            return;
        }

        @Nullable CompoundTag attachmentCustomDataTag = iGun.getAttachmentCustomDataTag(gunItem, this.category);
        if (attachmentCustomDataTag == null) return;

        { // 写入操作需要保存
            AttachmentNBTAccessor.INSTANCE.setLaserColor(attachmentCustomDataTag, rgb_new);
        }
        iGun.setAttachmentCustomDataTag(gunItem, this.category, attachmentCustomDataTag);
    }

    /**
     * 跟{@link BeamRender#_getLaser}保持一致
     */
    private int getColor(AttachmentCategory category) {
        if (inventory == null) {
            return Color64._FF0000.getRGB();
        }
        ItemStack gunItem = inventory.getItem(gunItemIndex);
        @Nullable IGun iGun = IGunGetter.fromItemStack(gunItem);

        if (iGun == null) return Color64._FF0000.getRGB();

        if (category == AttachmentCategory.NONE)
        { // 枪械激光
            if (iGun.hasLaserColor(gunItem)) {
                return iGun.getLaserColorInt(gunItem);
            }

            @Nullable GunDisplayInstance gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(gunItem);
            if (gunDisplayInstance != null) {
                GunDisplay gunDisplay = gunDisplayInstance.getPojo();
                @Nullable _LaserDisplay laserDisplay = gunDisplay.getLaserDisplay();
                if (laserDisplay != null) {
                    return laserDisplay.getDefaultColor().getRGB();
                }
            }
        }

        { // 配件激光
            ItemStack attachmentItem = iGun.getAttachment(gunItem, category);
            @Nullable IAttachment iAttachment = IAttachmentGetter.fromItemStack(attachmentItem);
            if (iAttachment != null) {
                if (iAttachment.hasLaserColor(attachmentItem)) {
                    return iAttachment.getLaserColorInt(attachmentItem);
                }

                var attachmentLocation = iAttachment.getAttachmentLocation(attachmentItem);
                @Nullable ClientAttachmentIndexInstance clientAttachmentIndexInstance = ClientResourceApi.getClientAttachmentIndexInstance(attachmentLocation);
                if (clientAttachmentIndexInstance != null) {
                    AttachmentDisplay attachmentDisplay = clientAttachmentIndexInstance.getAttachmentDisplay();
                    @Nullable _LaserDisplay laserDisplay = attachmentDisplay.getLaserDisplay();
                    if (laserDisplay != null) {
                        return laserDisplay.getDefaultColor().getRGB();
                    }
                }
            }
        }

        return Color64._FF0000.getRGB();
    }

    /**
     * @deprecated forge leak
     */
    public static class LaserColorSlider extends AbstractSliderButton {
        private final HSVSliderGroup parent;

        public LaserColorSlider(int x, int y, int width, int height, Component component, double value, HSVSliderGroup parent) {
            super(x, y, width, height, component, value);
            this.parent = parent;
        }

        public double getValue() {
            return this.value;
        }

        // --------AbstractSliderButton--------

        @Override
        protected void updateMessage() {
        }

        @Override
        protected void applyValue() {
            parent.apply();
        }
    }
}
