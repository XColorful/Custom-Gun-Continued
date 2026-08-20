/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.gun;

import dev.xcolorful.customgun.client.api.gui.tooltip.BaseTooltipContext;
import dev.xcolorful.customgun.client.api.gui.tooltip.BaseTooltipView;
import dev.xcolorful.customgun.client.api.item.gun.GunTooltipMask;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.gui.tooltip.gun.GunTooltip;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class ClientGunTooltip implements ClientTooltipComponent {

    public final View view;
    private final Context context;

    public ClientGunTooltip(@NotNull GunTooltip gunTooltip) {
        this.view = new View();
        this.context = new Context(this.view, gunTooltip);
    }

    // --------ClientTooltipComponent--------

    @Override
    public int getHeight(@NotNull Font font) {
        return this.context.getHeight(font);
    }
    @Override
    public int getWidth(@NotNull Font font) {
        return this.context.getMaxWidth();
    }

    public void renderText(int startX, int startY) {
        int currentX = startX;
        int currentY = startY;

        for (GunTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderText(this.context,
                    currentX, currentY);

            currentY += mask.getTooltipPart().measureHeight(this.context);
        }
    }
    public void renderImage(int startX, int startY) {
        int currentX = startX;
        int currentY = startY;

        for (GunTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderImage(this.context,
                    currentX, currentY);

            currentY += mask.getTooltipPart().measureHeight(this.context);
        }
    }

    // --------record--------

    @ApiStatus.Internal
    public static final class View extends BaseTooltipView {
        public @Nullable List<FormattedCharSequence> desc;
        public @Nullable Component ammoName;
        public @Nullable Component ammoCount;
        public View() {
        }
    }
    @ApiStatus.Internal
    public static final class Context extends BaseTooltipContext<View> {
        public final @NotNull GunTooltip gunTooltip;
        public final @NotNull EnumSet<GunTooltipMask> visibleParts;
        public @Nullable GunIndexInstance gunIndexInstance;
        public @Nullable GunDisplayInstance gunDisplayInstance;
        public @Nullable GunpackInfo gunpackInfo;
        public boolean showCategory = false;
        public boolean showPackInfo = false;
        public boolean showPojoLocation = false;
        public Context(@NotNull View view, @NotNull GunTooltip gunTooltip) {
            super(view);
            this.gunTooltip = gunTooltip;
            IGun iGun = gunTooltip.iGun();
            ItemStack gunItem = gunTooltip.gunItem();
            this.visibleParts = GunTooltipMask.fromBitmap(iGun.getTooltipMask(gunItem));
            this.gunIndexInstance = ResourceApi.getGunIndexInstance(gunTooltip.gunLocation());
            this.gunDisplayInstance = ClientResourceApi.getGunDisplayInstance(iGun.getGunDisplayLocation(gunItem));
            this.gunpackInfo = ClientResourceApi.getGunpackInfo(gunTooltip.gunLocation().getNamespace());
            this.buildView();
        }
        @Override
        protected void buildView() {
            Font font = Minecraft.getInstance().font;
            for (GunTooltipMask mask : this.visibleParts) {
                mask.getTooltipPart().build(this, font);
            }
        }
        @Override
        protected int calculateHeight(Font font) {
            int height = 0;
            for (GunTooltipMask mask : this.visibleParts) {
                height += mask.getTooltipPart().measureHeight(this);
            }
            return height;
        }
    }

    // --------Compat--------
    // 跨版本适配层
    // 其他类可直接Ctrl CV

//    @Override
    public int getHeight() {
        return this.getHeight(Minecraft.getInstance().font);
    }

    @Override
    public void renderText(@NotNull GuiGraphics guiGraphics,
                           @NotNull Font font,
                           int startX, int startY) {
        { // 设置缓存
            this.context.textFont = font;
            this.context.textGraphic = guiGraphics;
//            this.context.textMatrix4f = matrix4f;
//            this.context.textBufferSource = bufferSource;

            this.renderText(startX, startY);
        }
        this.context._clearTextCache();
    }

    @Override
    public void renderImage(@NotNull Font font,
                            int startX, int startY,
                            int width, int height,
                            @NotNull GuiGraphics guiGraphics) {
        { // 设置缓存
            this.context.imageFont = font;
            this.context.imageGraphic = guiGraphics;

            this.renderImage(startX, startY);
        }
        this.context._clearImageCache();
    }
}
