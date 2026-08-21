/*
 * Copyright (c) 2024-2026 MCModderAnchor (https://github.com/MCModderAnchor)
 * SPDX-License-Identifier: GPL-3.0-only
 *
 * Source: https://github.com/MCModderAnchor/TACZ
 */

package dev.xcolorful.customgun.client.gui.tooltip.attachment;

import dev.xcolorful.customgun.client.api.item.attachment.AttachmentTooltipMask;
import dev.xcolorful.customgun.client.config.RenderConfig;
import dev.xcolorful.customgun.client.gui.tooltip.AbstractTooltipPart;
import dev.xcolorful.customgun.client.resource.assets.info.GunpackInfo;
import dev.xcolorful.customgun.core.api.item.attachment.AttachmentCategory;
import dev.xcolorful.customgun.core.api.minecraft.Color64;
import dev.xcolorful.customgun.core.resource.data.index.AttachmentIndex;
import dev.xcolorful.customgun.core.resource.instance.data.AttachmentIndexInstance;
import dev.xcolorful.customgun.core.util.ComponentUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/*
原版tab分类
枪包名
资源位置
 */
public final class AttachmentDetailInfoPart extends AbstractTooltipPart implements AttachmentTooltipPart {
    public static final AttachmentDetailInfoPart INSTANCE = new AttachmentDetailInfoPart();
    private AttachmentDetailInfoPart() {}

    private static final Color64 _defaultCategoryColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final Color64 _defaultPackColor = Color64.fromChatFormatting(ChatFormatting.BLUE);
    private static final Color64 _defaultLocationColor = _defaultCategoryColor;

    @Override
    public void build(ClientAttachmentTooltip.Context context, Font font) {
        var attachmentLocation = context.attachmentTooltip.attachmentLocation();

        Component category; {
            AttachmentCategory attachmentCategory; {
                @Nullable AttachmentIndexInstance attachmentIndexInstance = context.attachmentIndexInstance;
                if (attachmentIndexInstance != null) {
                    AttachmentIndex attachmentIndex = attachmentIndexInstance.getPojo();
                    attachmentCategory = attachmentIndex.getAttachmentCategory();
                } else {
                    attachmentCategory = AttachmentCategory.NONE;
                }
            }

            category = attachmentCategory.getCategoryLang().copy();
        }
        context.view.category = category;
        context.widenMaxWidth(font.width(category));
        context.showCategory = context.isCreative;

        Component packInfo; {
            @Nullable GunpackInfo gunpackInfo = context.gunpackInfo;
            if (gunpackInfo != null) {
                packInfo = Component.translatable(gunpackInfo.getNameLang())
                        .withStyle(ChatFormatting.ITALIC);
            } else {
                packInfo = ComponentUtils.unknownTranslatableKey();
            }
        }
        context.view.packInfo = packInfo;
        context.widenMaxWidth(font.width(context.view.packInfo));
        context.showPackInfo = context.isCreative && context.isAdvanced;

        context.view.pojoLocation = Component.literal(attachmentLocation.toString());
        context.showPojoLocation = context.showPackInfo && RenderConfig.APPEND_RESOURCE_LOCATION_IN_TOOLTIP.get();
    }

    @Override
    public int measureHeight(ClientAttachmentTooltip.Context context) {
        if (!context.visibleParts.contains(AttachmentTooltipMask.DETAIL_INFO)) return 0;

        int height = 0;

        if (context.showCategory && context.view.category != null) height += textLineSpacing + textLineHeight;
        if (context.showPackInfo && context.view.packInfo != null) height += textLineSpacing + textLineHeight;
        if (context.showPojoLocation && context.view.pojoLocation != null) height += textLineSpacing + textLineHeight;

        return height;
    }

    @Override
    public void renderText(ClientAttachmentTooltip.Context context,
                           int startX, int startY) {
        int currentX = startX;
        int currentY = startY;

        if (context.showCategory && context.view.category != null) {
            currentY += textLineSpacing;

            // 配件类型
            context.drawText(context.view.category,
                    currentX, currentY,
                    _defaultCategoryColor.getRGB()
            );
            currentY += textLineHeight;
        }

        if (context.showPackInfo && context.view.packInfo != null) {
            currentY += textLineSpacing;

            // 枪包信息
            context.drawText(context.view.packInfo,
                    currentX, currentY,
                    _defaultPackColor.getRGB()
            );
            currentY += textLineHeight;
        }

        if (context.showPojoLocation && context.view.pojoLocation != null) {
            currentY += textLineSpacing;

            // 资源位置
            context.drawText(context.view.pojoLocation,
                    currentX, currentY,
                    _defaultLocationColor.getRGB()
            );
            currentY += textLineHeight;
        }
    }

    @Override
    public void renderImage(ClientAttachmentTooltip.Context context,
                            int startX, int startY) {
        // mixin注入点
    }
}
