/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.attachment;

import dev.xcolorful.customgun.client.api.item.attachment.AttachmentTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

/**
 * "[Shift] 展开枪械安装性"预计放在这里，用于提示
 * 目前打算放扩展模组里实现
 * 可以实现类别、哪些可见(例如未解锁进度就隐藏枪械)，是否非创造模式可见
 */
public final class AttachmentGuideTipPart extends AbstractTooltipPart implements AttachmentTooltipPart {
    public static final AttachmentGuideTipPart INSTANCE = new AttachmentGuideTipPart();
    private AttachmentGuideTipPart() {}

    @Override
    public void build(ClientAttachmentTooltip.Context context) {
        // mixin注入点

        context.showGunInstallability = context.isCreative && context.isAdvanced;

        // mixin注入点
    }

    @Override
    public int measureHeight(ClientAttachmentTooltip.Context context) {
        if (!context.visibleParts.contains(AttachmentTooltipMask.GUIDE_TIP)) return 0;

        // mixin注入点
        return 0;
    }

    @Override
    public void renderText(ClientAttachmentTooltip.Context context,
                           Font font,
                           int pX, int pY,
                           Matrix4f matrix4f,
                           MultiBufferSource.BufferSource bufferSource) {
        // mixin注入点
    }

    @Override
    public void renderImage(ClientAttachmentTooltip.Context context,
                            Font font,
                            int pX,
                            int pY,
                            GuiGraphics guiGraphics) {
        // mixin注入点
    }
}
