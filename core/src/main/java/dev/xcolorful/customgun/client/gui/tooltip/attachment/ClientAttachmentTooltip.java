/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.attachment;

import dev.xcolorful.customgun.client.api.gui.tooltip.BaseTooltipContext;
import dev.xcolorful.customgun.client.api.gui.tooltip.BaseTooltipView;
import dev.xcolorful.customgun.core.gui.tooltip.attachment.AttachmentTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.List;

public class ClientAttachmentTooltip implements ClientTooltipComponent {

    public final View view;
    private final Context context;

    public ClientAttachmentTooltip(AttachmentTooltip attachmentTooltip) {
        this.view = new View();
        this.context = new Context(this.view, attachmentTooltip);
    }

    // --------ClientTooltipComponent--------

    @Override public int getHeight() {
        return this.context.getHeight();
    }
    @Override public int getWidth(Font font) {
        return this.context.getMaxWidth();
    }
    @Override
    public void renderText(@NotNull Font font,
                           int pX, int pY,
                           @NotNull Matrix4f matrix4f,
                           @NotNull MultiBufferSource.BufferSource bufferSource) {
        int currentY = pY;

    }
    @Override
    public void renderImage(@NotNull Font font,
                            int pX, int pY,
                            @NotNull GuiGraphics guiGraphics) {
        int currentY = pY;

    }

    // --------record--------

    @ApiStatus.Internal
    public static final class View extends BaseTooltipView {
        public @Nullable List<Component> attachmentProperties;
        public @Nullable MutableComponent tips;
        public @Nullable MutableComponent gunInstallability;
        public @Nullable List<ItemStack> installableGunItems;
        public View() {
        }
    }
    @ApiStatus.Internal
    public static final class Context extends BaseTooltipContext<View> {
        public final AttachmentTooltip attachmentTooltip;
        public Context(@NotNull View view, AttachmentTooltip attachmentTooltip) {
            super(view);
            this.attachmentTooltip = attachmentTooltip;
            this.buildView();
        }
        @Override
        protected void buildView() {
        }
        @Override
        protected int calculateHeight() {
            return 0;
        }
    }
}
