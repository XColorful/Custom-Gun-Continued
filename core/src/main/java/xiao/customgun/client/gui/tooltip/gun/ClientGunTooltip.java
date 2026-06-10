/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package xiao.customgun.client.gui.tooltip.gun;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
import xiao.customgun.client.api.item.gun.GunTooltipMask;
import xiao.customgun.client.api.resource.ClientResourceApi;
import xiao.customgun.client.api.gui.tooltip.BaseTooltipContext;
import xiao.customgun.client.api.gui.tooltip.BaseTooltipView;
import xiao.customgun.client.resource.instance.assets.GunDisplayInstance;
import xiao.customgun.core.api.item.IGun;
import xiao.customgun.core.api.resource.ResourceApi;
import xiao.customgun.core.gui.tooltip.gun.GunTooltip;
import xiao.customgun.core.resource.instance.data.GunIndexInstance;

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

    @Override public int getHeight(Font font) {
        return this.context.getHeight();
    }
    @Override public int getWidth(Font font) {
        return this.context.getMaxWidth();
    }
    @Override
    public void extractText(GuiGraphicsExtractor guiGraphics,
                           Font font, int pX, int pY) {
        for (GunTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderText(this.context,
                    guiGraphics,
                    font, pX, pY);
        }
    }
    @Override
    public void extractImage(Font font, int pX, int pY,
                            int width, int height,
                            GuiGraphicsExtractor guiGraphics) {
        for (GunTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderImage(this.context,
                    font, pX, pY,
                    width, height,
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
