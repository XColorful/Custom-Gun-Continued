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
import net.minecraft.network.chat.MutableComponent;
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
    @Override public int getWidth(Font font) {
        return this.context.getMaxWidth();
    }
    @Override
    public void renderText(Font font, int pX, int pY,
                           Matrix4f matrix4f, MultiBufferSource.BufferSource bufferSource) {
        for (GunTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderText(this.context,
                    font, pX, pY,
                    matrix4f, bufferSource);
        }
    }
    @Override
    public void renderImage(Font font, int pX, int pY,
                            GuiGraphics guiGraphics) {
        for (GunTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderImage(this.context,
                    font, pX, pY,
                    guiGraphics);
        }
    }

    // --------record--------

    @ApiStatus.Internal
    public static final class View extends BaseTooltipView {
        public @Nullable List<FormattedCharSequence> desc;
        public @Nullable Component ammoName;
        public @Nullable MutableComponent ammoCount;
        public @Nullable MutableComponent gunCategory;
        public @Nullable MutableComponent damage;
        public @Nullable MutableComponent armorIgnore;
        public @Nullable MutableComponent headshotMultiplier;
        public @Nullable MutableComponent weight;
        public @Nullable MutableComponent tips;
        public @Nullable MutableComponent gunLevel;
        public View() {
        }
    }
    @ApiStatus.Internal
    public static final class Context extends BaseTooltipContext<View> {
        public final @NotNull GunTooltip gunTooltip;
        public final @NotNull EnumSet<GunTooltipMask> visibleParts;
        public @Nullable GunIndexInstance gunIndexInstance;
        public @Nullable GunDisplayInstance gunDisplayInstance;
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
