/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.attachment;

import dev.xcolorful.customgun.client.api.gui.tooltip.BaseTooltipContext;
import dev.xcolorful.customgun.client.api.gui.tooltip.BaseTooltipView;
import dev.xcolorful.customgun.client.api.item.attachment.AttachmentTooltipMask;
import dev.xcolorful.customgun.client.api.resource.ClientResourceApi;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.item.IAttachment;
import dev.xcolorful.customgun.core.api.resource.ResourceApi;
import dev.xcolorful.customgun.core.gui.tooltip.attachment.AttachmentTooltip;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class ClientAttachmentTooltip implements ClientTooltipComponent {

    public final View view;
    private final Context context;

    public ClientAttachmentTooltip(@NotNull AttachmentTooltip attachmentTooltip) {
        this.view = new View();
        this.context = new Context(this.view, attachmentTooltip);
    }

    // --------ClientTooltipComponent--------

    @Override public int getHeight(Font font) {
        return this.context.getHeight();
    }
    @Override public int getWidth(@NotNull Font font) {
        return this.context.getMaxWidth();
    }
    @Override
    public void extractText(@NotNull GuiGraphicsExtractor guiGraphics,
                           @NotNull Font font,
                           int pX, int pY) {
        int currentX = pX;
        int currentY = pY;

        for (AttachmentTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderText(this.context,
                    font,
                    currentX, currentY,
                    matrix4f,
                    bufferSource);

            currentY += mask.getTooltipPart().measureHeight(this.context);
        }
    }
    @Override
    public void extractImage(@NotNull Font font,
                            int pX, int pY,
                            int width, int height,
                            @NotNull GuiGraphicsExtractor guiGraphics) {
        int currentX = pX;
        int currentY = pY;

        for (AttachmentTooltipMask mask : this.context.visibleParts) {
            mask.getTooltipPart().renderImage(this.context,
                    font,
                    currentX, currentY,
                    guiGraphics);

            currentY += mask.getTooltipPart().measureHeight(this.context);
        }
    }

    // --------record--------

    @ApiStatus.Internal
    public static final class View extends BaseTooltipView {
        public @Nullable List<FormattedCharSequence> desc;
        public @Nullable List<ItemStack> installableGunItems;
        public View() {
        }
    }
    @ApiStatus.Internal
    public static final class Context extends BaseTooltipContext<View> {
        public final @NotNull AttachmentTooltip attachmentTooltip;
        public final @NotNull EnumSet<AttachmentTooltipMask> visibleParts;
        public @Nullable AttachmentIndexInstance attachmentIndexInstance;
        public @Nullable GunpackInfo gunpackInfo;
        public boolean showGunInstallability = false;
        public boolean showCategory = false;
        public boolean showPackInfo = false;
        public boolean showPojoLocation = false;
        public Context(@NotNull View view, @NotNull AttachmentTooltip attachmentTooltip) {
            super(view);
            this.attachmentTooltip = attachmentTooltip;
            IAttachment iAttachment = attachmentTooltip.iAttachment();
            ItemStack attachmentItem = attachmentTooltip.attachmentItem();
            this.visibleParts = AttachmentTooltipMask.fromBitmap(iAttachment.getTooltipMask(attachmentItem));
            this.attachmentIndexInstance = ResourceApi.getAttachmentIndexInstance(attachmentTooltip.attachmentLocation());
            this.gunpackInfo = ClientResourceApi.getGunpackInfo(attachmentTooltip.attachmentLocation().getNamespace());
            this.buildView();
        }
        @Override
        protected void buildView() {
            for (AttachmentTooltipMask mask : this.visibleParts) {
                mask.getTooltipPart().build(this);
            }
        }
        @Override
        protected int calculateHeight() {
            int height = 0;
            for (AttachmentTooltipMask mask : this.visibleParts) {
                height += mask.getTooltipPart().measureHeight(this);
            }
            return height;
        }
    }
}
