/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.attachment;

import dev.xcolorful.customgun.client.api.item.attachment.AttachmentTooltipMask;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/*
描述内容，描述内容
，描述内容。
 */
public final class AttachmentDescriptionPart extends AbstractTooltipPart implements AttachmentTooltipPart {
    public static final AttachmentDescriptionPart INSTANCE = new AttachmentDescriptionPart();
    private AttachmentDescriptionPart() {}

    /**
     * 扩展模组可以动态识别当前鼠标到边界的距离来调整
     */
    @ApiStatus.Internal public static volatile int _textLineWidth = 300;
    private static final Color64 _defaultDescriptionColor = Color64.fromChatFormatting(ChatFormatting.GRAY);

    @Override
    public void build(ClientAttachmentTooltip.Context context, Font font) {
        @Nullable AttachmentIndexInstance attachmentIndexInstance = context.attachmentIndexInstance;
        if (attachmentIndexInstance == null) return;

        List<FormattedCharSequence> desc; {
            AttachmentIndex attachmentIndex = attachmentIndexInstance.getPojo();
            String tooltipLang = attachmentIndex.getTooltipLang();

            desc = font.split(Component.translatable(tooltipLang), _textLineWidth);
        }
        context.view.desc = desc;
        for (int i = 0; i < desc.size(); i++) {
            FormattedCharSequence sequence = desc.get(i);
            context.widenMaxWidth(font.width(sequence));
        }
    }

    @Override
    public int measureHeight(ClientAttachmentTooltip.Context context) {
        if (!context.visibleParts.contains(AttachmentTooltipMask.DESCRIPTION)) return 0;

        @Nullable List<FormattedCharSequence> desc = context.view.desc;
        if (desc == null || desc.isEmpty()) return 0;

        return
//                textLineSpacing +
                        desc.size() * textLineHeight;
    }

    @Override
    public void renderText(ClientAttachmentTooltip.Context context,
                           int startX, int startY) {
        int currentX = startX;
        int currentY = startY;

        @Nullable List<FormattedCharSequence> desc = context.view.desc;
        if (desc != null) {
            /**
             * {@link AttachmentTooltipMask}第一项前不加行间距，后面的不管
             */
//            yOffset += textLineSpacing;

            // 配件描述
            for (int i = 0; i < desc.size(); i++) {
                // 单行配件描述
                FormattedCharSequence sequence = desc.get(i);
                context.drawText(sequence,
                        currentX, currentY,
                        _defaultDescriptionColor.getRGB()
                );
                currentY += textLineHeight;
            }
        }
    }
}
