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
import dev.xcolorful.customgun.client.resource.instance.assets.GunDisplayInstance;
import dev.xcolorful.customgun.core.api.item.IGun;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.gui.tooltip.gun.GunTooltip;
import dev.xcolorful.customgun.core.resource.instance.data.GunIndexInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

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

    @Override public int getHeight() {
        return this.context.getHeight();
    }
    @Override public int getWidth(@NotNull Font font) {
        return this.context.getMaxWidth();
    }
    @Override
    public void renderText(@NotNull Font font,
                           int pX, int pY,
                           @NotNull Matrix4f matrix4f,
                           @NotNull MultiBufferSource.BufferSource bufferSource) {
        int currentY = pY;

        for (GunTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderText(this.context,
                    font,
                    pX, currentY,
                    matrix4f,
                    bufferSource);

            currentY += mask.getTooltipPart().measureHeight(this.context);
        }
    }
    @Override
    public void renderImage(@NotNull Font font,
                            int pX, int pY,
                            @NotNull GuiGraphics guiGraphics) {
        int currentY = pY;

        for (GunTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderImage(this.context,
                    font,
                    pX, currentY,
                    guiGraphics);

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
            this.buildView();
        }
        @Override
        protected void buildView() {
            for (GunTooltipMask mask : this.visibleParts) {
                mask.getTooltipPart().build(this);
            }
        }
        @Override
        protected int calculateHeight() {
            int height = 0;
            for (GunTooltipMask mask : this.visibleParts) {
                height += mask.getTooltipPart().measureHeight(this);
            }
            return height;
        }
    }
}
